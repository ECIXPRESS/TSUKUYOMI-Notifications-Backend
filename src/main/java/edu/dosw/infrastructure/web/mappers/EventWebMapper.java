package edu.dosw.infrastructure.web.mappers;

import edu.dosw.application.dto.command.NotificationCommand;
import org.springframework.stereotype.Component;
import java.util.Map;

@Component
public class EventWebMapper {

    public NotificationCommand toCommand(Map<String, Object> request) {
        NotificationCommand command = new NotificationCommand();

        if (request.containsKey("userId")) command.setUserId((String) request.get("userId"));
        if (request.containsKey("email")) command.setEmail((String) request.get("email"));
        if (request.containsKey("name")) command.setName((String) request.get("name"));
        if (request.containsKey("ip")) command.setIp((String) request.get("ip"));
        if (request.containsKey("orderId")) command.setOrderId((String) request.get("orderId"));
        if (request.containsKey("orderStatus")) command.setOrderStatus((String) request.get("orderStatus"));
        if (request.containsKey("pointOfSaleId")) command.setPointOfSaleId((String) request.get("pointOfSaleId"));
        if (request.containsKey("pickupTime")) command.setPickupTime((String) request.get("pickupTime"));

        return command;
    }
}