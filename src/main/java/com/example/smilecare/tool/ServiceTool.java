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

    @Tool(name = "list_all_services", description = "Liệt kê tất cả dịch vụ nha khoa và bảng giá của phòng khám")
    public String listAllServices() {
        return formatServices(serviceCatalogService.findAll());
    }

    @Tool(name = "search_services", description = "Tìm kiếm dịch vụ nha khoa theo tên hoặc mô tả")
    public String searchServices(@ToolParam(description = "Từ khóa tìm kiếm dịch vụ") String keyword) {
        return formatServices(serviceCatalogService.search(keyword));
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