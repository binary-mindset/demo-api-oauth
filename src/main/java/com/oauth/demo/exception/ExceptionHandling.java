package com.oauth.demo.exception;

import net.logstash.logback.marker.LogstashMarker;
import net.logstash.logback.marker.Markers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.NativeWebRequest;
import org.zalando.problem.DefaultProblem;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.spring.web.advice.ProblemHandling;
import org.zalando.problem.spring.web.advice.security.SecurityAdviceTrait;
import org.zalando.problem.violations.ConstraintViolationProblem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Handles all exceptions thrown out of RestControllers.
 * It converts the exception into a Json Problem object (done by zalando's problem-spring-web library)
 * and logs the exception, in case of json logstash logging enriched with Markers containing addtional
 * request information like path, session id and tracking id.
 */
@ControllerAdvice
public class ExceptionHandling implements ProblemHandling, SecurityAdviceTrait {

    private static final String FIELD_ERRORS_KEY = "fieldErrors";
    private static final String ERROR_CODE_KEY = "errorcode";
    private static final String TIMESTAMP_KEY = "timestamp";
    private static final String MESSAGE_KEY = "message";
    private static final String PATH_KEY = "path";
    private static final String VIOLATIONS_KEY = "violations";

    /**
     * Post-process the Problem payload to add the message key for the front-end if needed
     */
    @Override
    public ResponseEntity<Problem> process(@Nullable ResponseEntity<Problem> entity, NativeWebRequest request) {
        if (entity == null) {
            return entity;
        }
        Problem problem = entity.getBody();
        if (!(problem instanceof ConstraintViolationProblem || problem instanceof DefaultProblem)) {
            return entity;
        }
        ProblemBuilder builder = Problem.builder()
                .withType(Problem.DEFAULT_TYPE.equals(problem.getType()) ? ErrorConstants.DEFAULT_TYPE : problem.getType())
                .withStatus(problem.getStatus())
                .withTitle(problem.getTitle())
                .with(PATH_KEY, request.getNativeRequest(HttpServletRequest.class).getRequestURI());

        if (problem instanceof ConstraintViolationProblem) {
            builder
                    .with(VIOLATIONS_KEY, ((ConstraintViolationProblem) problem).getViolations())
                    .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION);
        } else {
            builder
                    .withCause(((DefaultProblem) problem).getCause())
                    .withDetail(problem.getDetail())
                    .withInstance(problem.getInstance());
        }
        problem.getParameters().forEach(builder::with);
        if (!problem.getParameters().containsKey(MESSAGE_KEY) && problem.getStatus() != null) {
            builder.with(MESSAGE_KEY, "error.http." + problem.getStatus().getStatusCode());
        }
        return new ResponseEntity<>(builder.build(), entity.getHeaders(), entity.getStatusCode());
    }

    @Override
    public ResponseEntity<Problem> handleMethodArgumentNotValid(MethodArgumentNotValidException ex, @Nonnull NativeWebRequest request) {
        BindingResult result = ex.getBindingResult();
        List<FieldErrorVM> fieldErrors = result.getFieldErrors().stream()
                .map(f -> new FieldErrorVM(f.getObjectName(), f.getField(), f.getCode()))
                .collect(Collectors.toList());

        Problem problem = Problem.builder()
                .withType(ErrorConstants.CONSTRAINT_VIOLATION_TYPE)
                .withTitle("Method argument not valid")
                .withStatus(defaultConstraintViolationStatus())
                .with(MESSAGE_KEY, ErrorConstants.ERR_VALIDATION)
                .with(FIELD_ERRORS_KEY, fieldErrors)
                .build();
        return create(ex, problem, request);
    }

    @Override
    public void log(Throwable throwable, Problem problem, NativeWebRequest request, HttpStatus status) {
        HttpServletRequest req = request.getNativeRequest(HttpServletRequest.class);
        Logger log = LoggerFactory.getLogger(getLoggerName(throwable));
        LogstashMarker logstashMarkers = Markers.appendEntries(getAdditionalFields(req));
        log.error(logstashMarkers, problem.getDetail(), throwable);
    }

    /**
     * picks the name of first class in the stackstrace as logger name.
     * if none is present, it chooses ExceptionHandling class name as logger name
     */
    protected String getLoggerName(Throwable t) {
        return t != null && t.getStackTrace() != null
                && t.getStackTrace()[0] != null ? t.getStackTrace()[0].getClassName() : getClass().getName();
    }

    static final String REQUEST_URI = "request";

    /**
     * returns a map containing additional information out of request.
     *
     * @param request original http servlet request
     * @return Map map containing additional information as String key and Object value pairs
     */
    protected Map<String, Object> getAdditionalFields(HttpServletRequest request) {
        Map<String, Object> map = new HashMap<>();

        putEntry(map, REQUEST_URI, request.getRequestURI());

        return map;
    }

    /*
     * helper to create a Map putEntry object from a key and a value.
     * Avoids NPEs by setting the String "<unset>" for null values.
     */
    private static void putEntry(Map<String, Object> map, String key, Object value) {
        map.put(key, value != null ? value : "<unset>");
    }
}
