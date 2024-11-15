package com.onepoint;

public sealed interface AuthenticationState {
    record Anonymous() implements AuthenticationState {}

    record Authenticated(String username, boolean isAdmin) implements AuthenticationState {}

    record AuthError(int errorCode) implements AuthenticationState {}
}
