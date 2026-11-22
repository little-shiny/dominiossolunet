package com.dominiossolunet.dto;

import java.util.List;

public record ConfirmacionRenovacionRequest(
        String token,
        List<Integer> dominios
) {
}