package org.example.model;

import lombok.Getter;
import lombok.Setter;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Setter
@Getter
public class ClientForm {

    @NotBlank(message = "Client name is required")
    @Size(max = 255, message = "Client name must be at most 255 characters")
    private String clientName;
    
    private Boolean status = true; // Default to true (active)
}
