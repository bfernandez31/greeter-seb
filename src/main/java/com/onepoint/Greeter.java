package com.onepoint;

public class Greeter {
    public String getGreetingFromAuthenticationState(AuthenticationState state) {
        return switch (state) {
            case AuthenticationState.Anonymous() -> "Welcome guest!";
            case AuthenticationState.Authenticated(String username, boolean isAdmin) -> {
                if (isAdmin && username.equals("root")) {
                    yield "Greetings grand master!";
                }
                yield (isAdmin ? "Hi " : "Hello ") + username + "!";
            }
            case AuthenticationState.AuthError(int code) -> switch (code) {
                case 401 -> "Oops, couldn't log you in (reason: bad credentials).";
                case 403 -> "Sorry, your account has been disabled.";
                case 404 -> "Sorry, this account has been deleted or doesn't exist.";
                case 503 -> "Impossible to connect to the authentication server.";
                default -> "An unknown error happened.";
            };
        };
    }
}
