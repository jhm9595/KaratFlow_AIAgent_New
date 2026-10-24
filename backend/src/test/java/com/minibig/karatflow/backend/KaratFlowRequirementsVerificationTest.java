package com.minibig.karatflow.backend;

import com.minibig.karatflow.backend.domain.*;
import com.minibig.karatflow.backend.dto.OrderCreateRequestDTO;
import com.minibig.karatflow.backend.dto.OrderResponseDTO;
import com.minibig.karatflow.backend.repository.*;
import com.minibig.karatflow.backend.service.KakaoBotService;
import com.minibig.karatflow.backend.service.OrderService;
import com.minibig.karatflow.backend.service.ProductService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class KaratFlowRequirementsVerificationTest {

    @Autowired
    private OrderService orderService;

    @Autowired
    private ProductService productService;

    @Autowired
    private KakaoBotService kakaoBotService;

    @Autowired
    private ProcessTemplateRepository templateRepository;

    @Autowired
    private OrderRepository orderRepository;

    @Autowired
    private WorkOrderRepository workOrderRepository;

    @Autowired
    private OrderItemRepository orderItemRepository;

    private ProcessTemplate createCustomTemplate() {
        ProcessTemplate template = ProcessTemplate.builder()
                .templateCode("TPL_CUSTOM_V21")
                .templateName("맞춤 세공 파이프라인")
                .isDefault(true)
                .steps(new ArrayList<>())
                .build();
        template = templateRepository.save(template);

        List<ProcessTemplateStep> steps = List.of(
                ProcessTemplateStep.builder().template(template).stepOrder(1).stageCode(null).stageName("접수").colorHex("#3B82F6").build(),
                ProcessTemplateStep.builder().template(template).stepOrder(2).stageCode(null).stageName("캐드").colorHex("#8B5CF6").build(),
                ProcessTemplateStep.builder().template(template).stepOrder(3).stageCode(null).stageName("출고").colorHex("#10B981").build()
        );
        template.getSteps().addAll(steps);
        return templateRepository.save(template);
    }

    @Test
    @DisplayName("1. OrderStatus ENUM Normalization: 한글/구형 상태값을 PROCESSING, COMPLETED, CANCELLED 3대 표준 ENUM으로 강제 정규화")
    void verifyOrderStatusEnumNormalization() {
        assertThat(OrderStatus.normalize("진행중")).isEqualTo("PROCESSING");
        assertThat(OrderStatus.normalize(null)).isEqualTo("PROCESSING");
        assertThat(OrderStatus.normalize("완료")).isEqualTo("COMPLETED");
        assertThat(OrderStatus.normalize("DONE")).isEqualTo("COMPLETED");
        assertThat(OrderStatus.normalize("COMPLETED")).isEqualTo("COMPLETED");
        assertThat(OrderStatus.normalize("취소")).isEqualTo("CANCELLED");
        assertThat(OrderStatus.normalize("CANCELLED")).isEqualTo("CANCELLED");

        Order order = Order.builder().status("완료").build();
        order.ensureValidStatus();
        assertThat(order.getStatus()).isEqualTo("COMPLETED");
    }

    @Test
    @DisplayName("2. Order 1:N WorkOrder Lifecycle & Custom stageName Preservation: 수량별 개별 지시서 생성, 병목 공정 계산, 최종 단계 도달 시 COMPLETED 승격 및 물리적 공정명('출고') 보존")
    void verifyOrderWorkOrderLifecycleAndStagePreservation() {
        ProcessTemplate tpl = createCustomTemplate();

        OrderCreateRequestDTO req = new OrderCreateRequestDTO();
        req.setOrderType("B2B");
        req.setCustomerName("종로귀금속");
        req.setCustomerPhone("010-1234-5678");
        req.setQuantity(2);
        req.setProcessTemplateId(tpl.getId());
        req.setUnmappedBrandName("카랏골드");
        req.setUnmappedProductName("18K 시그니처 커플링");
        req.setFinalConsumerPrice(550000.0);

        OrderResponseDTO created = orderService.createOrder(req);
        assertThat(created.getStatus()).isEqualTo("PROCESSING");
        assertThat(created.getStage()).isEqualTo("접수");
        assertThat(created.getQuantity()).isEqualTo(2);

        List<WorkOrder> wos = workOrderRepository.findAllByOrderId(created.getId());
        assertThat(wos).hasSize(2);
        WorkOrder wo1 = wos.get(0);
        WorkOrder wo2 = wos.get(1);

        // wo1만 '캐드'로 진전 -> wo2는 아직 '접수'이므로 주문 대표 공정(병목)은 '접수', 상태는 PROCESSING 유지
        OrderResponseDTO afterStep1 = orderService.advanceStage(wo1.getId());
        assertThat(afterStep1.getItemStage()).isEqualTo("캐드");
        assertThat(afterStep1.getStage()).isEqualTo("접수");
        assertThat(afterStep1.getStatus()).isEqualTo("PROCESSING");

        // wo1을 마지막 단계인 '출고'로 진전 -> wo2가 아직 '접수'이므로 주문은 여전히 PROCESSING
        OrderResponseDTO afterWo1Done = orderService.advanceStage(wo1.getId());
        assertThat(afterWo1Done.getItemStage()).isEqualTo("출고");
        assertThat(afterWo1Done.getAllFinished()).isFalse();
        assertThat(afterWo1Done.getStatus()).isEqualTo("PROCESSING");

        // wo2를 '캐드' -> '출고'로 진전시켜 모든 하위 WorkOrder 완료 달성
        orderService.advanceStage(wo2.getId());
        OrderResponseDTO afterAllDone = orderService.advanceStage(wo2.getId());

        // 모든 물건이 마지막 단계('출고')에 도달하면 주문 status는 COMPLETED로 승격하되, 물리적 stage는 '완료'로 덮어쓰지 않고 '출고'를 유지
        assertThat(afterAllDone.getAllFinished()).isTrue();
        assertThat(afterAllDone.getStatus()).isEqualTo("COMPLETED");
        assertThat(afterAllDone.getStage()).isEqualTo("출고");
    }

    @Test
    @DisplayName("3. Unmapped Product Candidate Queue & Approval/Mapping: 미매핑 제품 후보 조회 및 정식 제품 승인/매핑")
    void verifyUnmappedCandidateApprovalAndMapping() {
        ProcessTemplate tpl = createCustomTemplate();

        OrderCreateRequestDTO req = new OrderCreateRequestDTO();
        req.setOrderType("B2C");
        req.setCustomerName("홍길동");
        req.setQuantity(1);
        req.setProcessTemplateId(tpl.getId());
        req.setUnmappedBrandName("아틀리에");
        req.setUnmappedProductName("24K 순금 용반지");
        req.setImageUrl("/uploads/dragon-ring.jpg");

        OrderResponseDTO created = orderService.createOrder(req);
        List<OrderItem> candidates = productService.getUnmappedCandidates();
        OrderItem targetCandidate = candidates.stream()
                .filter(c -> c.getOrder().getId().equals(created.getId()))
                .findFirst()
                .orElseThrow();

        assertThat(targetCandidate.getDesign()).isNull();
        assertThat(targetCandidate.getUnmappedProductName()).isEqualTo("24K 순금 용반지");

        Design confirmed = productService.confirmProduct(
                targetCandidate.getId(),
                "아틀리에",
                "DRG-24K-001",
                "24K 순금 용반지",
                BigDecimal.valueOf(85000)
        );

        assertThat(confirmed.getId()).isNotNull();
        assertThat(confirmed.getDesignCode()).isEqualTo("DRG-24K-001");
        assertThat(orderItemRepository.findById(targetCandidate.getId()).orElseThrow().getDesign()).isNotNull();
    }

    @Test
    @DisplayName("4. Kakao OpenBuilder Webhook: 실시간 주문 공정 조회 및 취소 위약금 사전 계산 응답")
    void verifyKakaoWebhookOrderStatusAndCancelPreview() {
        ProcessTemplate tpl = createCustomTemplate();

        OrderCreateRequestDTO req = new OrderCreateRequestDTO();
        req.setOrderType("B2C");
        req.setCustomerName("김고객");
        req.setQuantity(1);
        req.setProcessTemplateId(tpl.getId());
        req.setUnmappedProductName("14K 데일리 목걸이");
        req.setFinalConsumerPrice(300000.0);

        OrderResponseDTO created = orderService.createOrder(req);
        Map<String, Object> payload = Map.of(
                "userRequest", Map.of("utterance", created.getOrderNo() + " 상태 알려줘")
        );

        Map<String, Object> statusResp = kakaoBotService.handleOrderStatusRequest(payload);
        assertThat(statusResp).containsEntry("version", "2.0");
        String statusText = extractKakaoText(statusResp);
        assertThat(statusText).contains(created.getOrderNo()).contains("접수").contains("PROCESSING");

        Map<String, Object> cancelResp = kakaoBotService.handleCancelPreviewRequest(payload);
        assertThat(cancelResp).containsEntry("version", "2.0");
        String cancelText = extractKakaoText(cancelResp);
        assertThat(cancelText).contains(created.getOrderNo()).contains("위약금");
    }

    @SuppressWarnings("unchecked")
    private String extractKakaoText(Map<String, Object> kakaoResponse) {
        Map<String, Object> template = (Map<String, Object>) kakaoResponse.get("template");
        Object[] outputs = (Object[]) template.get("outputs");
        Map<String, Object> firstOutput = (Map<String, Object>) outputs[0];
        Map<String, Object> simpleText = (Map<String, Object>) firstOutput.get("simpleText");
        return (String) simpleText.get("text");
    }
}
