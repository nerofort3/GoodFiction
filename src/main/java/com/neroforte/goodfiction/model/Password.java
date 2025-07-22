package com.neroforte.goodfiction.model;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Password {

    private String oldPassword;
    private String newPassword;
}
