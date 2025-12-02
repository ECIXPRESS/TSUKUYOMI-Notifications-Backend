package edu.dosw.application.dto.command;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class LoginEventCommand {
    private String userId;
    private String email;
    private String name;
    private String ip;
}