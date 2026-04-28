package com.plataforma.tramites.modules.tramites.service;

import com.plataforma.tramites.shared.exception.ApiException;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class TramiteFlujoCondicionEvaluatorTest {

    private final TramiteFlujoCondicionEvaluator evaluator = new TramiteFlujoCondicionEvaluator();

    @Test
    void evaluate_equalsAndNotEquals() {
        Map<String, String> ctx = Map.of("tramite.estado", "ACTIVO", "prioridad", "ALTA");
        assertTrue(evaluator.evaluate("tramite.estado == ACTIVO", ctx));
        assertTrue(evaluator.evaluate("prioridad != BAJA", ctx));
        assertFalse(evaluator.evaluate("tramite.estado == CERRADO", ctx));
    }

    @Test
    void evaluate_inAndExists() {
        Map<String, String> ctx = Map.of("prioridad", "MEDIA", "form.tipo", "json");
        assertTrue(evaluator.evaluate("prioridad in [BAJA, MEDIA, ALTA]", ctx));
        assertTrue(evaluator.evaluate("form.tipo exists", ctx));
        assertFalse(evaluator.evaluate("areaActualId exists", ctx));
    }

    @Test
    void evaluate_invalidExpression() {
        Map<String, String> ctx = Map.of("estado", "ACTIVO");
        ApiException invalidOp = assertThrows(ApiException.class, () -> evaluator.evaluate("estado >=", ctx));
        assertEquals(HttpStatus.BAD_REQUEST, invalidOp.getStatus());
        assertTrue(invalidOp.getMessage().contains("Operadores soportados"));

        ApiException invalidSyntax = assertThrows(ApiException.class, () -> evaluator.evaluate("in [A,B]", ctx));
        assertEquals(HttpStatus.BAD_REQUEST, invalidSyntax.getStatus());
    }

    @Test
    void evaluate_invalidFieldAndEmptyValues() {
        Map<String, String> ctx = Map.of("estado", "ACTIVO");

        ApiException invalidField = assertThrows(ApiException.class, () -> evaluator.evaluate("1estado == ACTIVO", ctx));
        assertTrue(invalidField.getMessage().contains("Campo inválido"));

        ApiException emptyEq = assertThrows(ApiException.class, () -> evaluator.evaluate("estado == ", ctx));
        assertTrue(emptyEq.getMessage().contains("Valor esperado vacío"));

        ApiException emptyIn = assertThrows(ApiException.class, () -> evaluator.evaluate("estado in []", ctx));
        assertTrue(emptyIn.getMessage().contains("lista de in no puede ser vacía"));
    }
}
