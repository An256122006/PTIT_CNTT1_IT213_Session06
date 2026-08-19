package com.example.smilecare.tool;

import com.example.smilecare.entity.DentalService;
import com.example.smilecare.service.ServiceCatalogService;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class ServiceTool {

    private final ServiceCatalogService serviceCatalogService;

    public ServiceTool(ServiceCatalogService serviceCatalogService) {
        this.serviceCatalogService = serviceCatalogService;
    }

    @Tool(name = "searchDentalServices",
            description = """
                    Tra cứu dịch vụ nha khoa tại phòng khám SmileCare.
                    Sử dụng khi khách hàng muốn biết danh sách dịch vụ,
                    tìm kiếm dịch vụ theo tên hoặc mô tả, hoặc hỏi về
                    giá cả của một dịch vụ nha khoa cụ thể (ví dụ:
                    niềng răng, tẩy trắng răng, bọc răng sứ, nhổ răng khôn,
                    cạo vôi, trám răng...).
                    """)
    public String searchDentalServices(
            @ToolParam(description = "Từ khóa tìm kiếm tên hoặc mô tả dịch vụ nha khoa (ví dụ: niềng răng, tẩy trắng, sứ)") String keyword) {
        List<DentalService> services = serviceCatalogService.search(keyword);
        return formatServices(services);
    }

    @Tool(name = "listAllDentalServices",
            description = """
                    Liệt kê toàn bộ dịch vụ nha khoa có tại phòng khám SmileCare.
                    Sử dụng khi khách hàng muốn xem danh sách tất cả các dịch vụ
                    hoặc chưa biết muốn sử dụng dịch vụ nào.
                    """)
    public String listAllDentalServices() {
        List<DentalService> services = serviceCatalogService.findAll();
        return formatServices(services);
    }

    private String formatServices(List<DentalService> services) {
        if (services.isEmpty()) {
            return "Không tìm thấy dịch vụ phù hợp.";
        }
        return services.stream()
                .map(s -> "ID: " + s.getId()
                        + " | " + s.getName()
                        + " | Mô tả: " + s.getDescription()
                        + " | Giá: " + s.getPrice() + " VNĐ"
                        + " | Thời gian: " + s.getDurationMinutes() + " phút")
                .collect(Collectors.joining("\n"));
    }
}
