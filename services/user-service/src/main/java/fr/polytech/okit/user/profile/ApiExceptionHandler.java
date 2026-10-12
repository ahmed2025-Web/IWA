package fr.polytech.okit.user.profile;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(UserProfileNotFoundException.class)
    ProblemDetail handleNotFound(UserProfileNotFoundException exception, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, exception.getMessage());
        problem.setTitle("Profil utilisateur introuvable");
        problem.setType(URI.create("urn:problem:user-profile-not-found"));
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ProblemDetail handleValidation(MethodArgumentNotValidException exception, HttpServletRequest request) {
        ProblemDetail problem = ProblemDetail.forStatusAndDetail(
                HttpStatus.BAD_REQUEST,
                "La requête contient des informations de profil invalides.");
        problem.setTitle("Requête invalide");
        problem.setType(URI.create("urn:problem:validation-error"));
        problem.setInstance(URI.create(request.getRequestURI()));
        return problem;
    }
}
