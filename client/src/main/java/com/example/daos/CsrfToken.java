package com.example.daos;

public record CsrfToken(String parameterName, String headerName, String token) {
}
