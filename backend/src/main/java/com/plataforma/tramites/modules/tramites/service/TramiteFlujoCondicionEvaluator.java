package com.plataforma.tramites.modules.tramites.service;

import com.plataforma.tramites.shared.exception.ApiException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Evaluador mínimo de condiciones de arista para flujo de trámites.
 *
 * <p>Soporta:
 * <ul>
 *   <li>{@code campo == valor}</li>
 *   <li>{@code campo != valor}</li>
 *   <li>{@code campo in [a,b,c]}</li>
 *   <li>{@code campo exists}</li>
 * </ul>
 */
@Component
public class TramiteFlujoCondicionEvaluator {

    private static final Pattern EXISTS_PATTERN =
            Pattern.compile("^\\s*([A-Za-z_][A-Za-z0-9_.]*)\\s+exists\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern IN_PATTERN =
            Pattern.compile("^\\s*([A-Za-z_][A-Za-z0-9_.]*)\\s+in\\s*\\[(.*)]\\s*$", Pattern.CASE_INSENSITIVE);
    private static final Pattern EQ_PATTERN =
            Pattern.compile("^\\s*([A-Za-z_][A-Za-z0-9_.]*)\\s*==\\s*(.+?)\\s*$");
    private static final Pattern NE_PATTERN =
            Pattern.compile("^\\s*([A-Za-z_][A-Za-z0-9_.]*)\\s*!=\\s*(.+?)\\s*$");
    private static final Pattern FIELD_PATTERN = Pattern.compile("^[A-Za-z_][A-Za-z0-9_.]*$");

    public boolean evaluate(String condicion, Map<String, String> contexto) {
        if (condicion == null || condicion.isBlank()) {
            return true;
        }
        String expr = condicion.trim();

        Matcher existsMatcher = EXISTS_PATTERN.matcher(expr);
        if (existsMatcher.matches()) {
            String campo = existsMatcher.group(1).trim();
            String valor = resolve(campo, contexto);
            return valor != null && !valor.isBlank();
        }

        Matcher inMatcher = IN_PATTERN.matcher(expr);
        if (inMatcher.matches()) {
            String campo = inMatcher.group(1).trim();
            String rawItems = inMatcher.group(2).trim();
            List<String> items = rawItems.isBlank()
                    ? List.of()
                    : Arrays.stream(rawItems.split(","))
                            .map(String::trim)
                            .map(this::stripQuotes)
                            .filter(s -> !s.isBlank())
                            .collect(Collectors.toList());
            if (items.isEmpty()) {
                throw invalid(condicion, "La lista de in no puede ser vacía; usar [A,B,...].");
            }
            String valor = resolve(campo, contexto);
            return valor != null && items.contains(valor);
        }

        Matcher eqMatcher = EQ_PATTERN.matcher(expr);
        if (eqMatcher.matches()) {
            String campo = eqMatcher.group(1).trim();
            String esperado = stripQuotes(eqMatcher.group(2).trim());
            if (esperado == null || esperado.isBlank()) {
                throw invalid(condicion, "Valor esperado vacío en operador ==.");
            }
            String valor = resolve(campo, contexto);
            return Objects.equals(valor, esperado);
        }

        Matcher neMatcher = NE_PATTERN.matcher(expr);
        if (neMatcher.matches()) {
            String campo = neMatcher.group(1).trim();
            String esperado = stripQuotes(neMatcher.group(2).trim());
            if (esperado == null || esperado.isBlank()) {
                throw invalid(condicion, "Valor esperado vacío en operador !=.");
            }
            String valor = resolve(campo, contexto);
            return !Objects.equals(valor, esperado);
        }

        validarPistaDeCampo(condicion, expr);
        throw invalid(condicion, "Operador no soportado.");
    }

    private String resolve(String campo, Map<String, String> contexto) {
        if (contexto.containsKey(campo)) {
            return contexto.get(campo);
        }
        return contexto.get("tramite." + campo);
    }

    private static void validarPistaDeCampo(String condicion, String expr) {
        String fieldCandidate = expr.split("\\s+")[0].trim();
        if (fieldCandidate.isBlank()) {
            throw invalid(condicion, "Expresión vacía.");
        }
        if (!FIELD_PATTERN.matcher(fieldCandidate).matches()) {
            throw invalid(condicion, "Campo inválido. Usar letras/números/_ y puntos (ej: tramite.estado, form.tipo).");
        }
    }

    private String stripQuotes(String input) {
        if (input == null) {
            return null;
        }
        String s = input.trim();
        if ((s.startsWith("\"") && s.endsWith("\"")) || (s.startsWith("'") && s.endsWith("'"))) {
            return s.substring(1, s.length() - 1);
        }
        return s;
    }

    private static ApiException invalid(String condicion, String detalle) {
        return new ApiException(
                HttpStatus.BAD_REQUEST,
                "CONDICION_INVALIDA: '" + condicion + "'. " + detalle
                        + " Operadores soportados: ==, !=, in [A,B], exists."
                        + " Ejemplos: prioridad == ALTA, form.tipo in [TECNICO,LEGAL], areaActualId exists.");
    }
}
