package com.onepoint;


import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;

class GreeterTest {


    public static Stream<Arguments> createAuthenticationStatesForTesting() {
        return Stream.of(
                Arguments.of(
                        Named.named(
                                "an unauthenticated user",
                                new AuthenticationState.Anonymous()
                        ),
                        "Welcome guest!"
                ),
                Arguments.of(
                        Named.named(
                                "an authenticated user called alice",
                                new AuthenticationState.Authenticated("alice", false)
                        ),
                        "Hello alice!"
                ),
                Arguments.of(
                        Named.named(
                                "an authenticated admin called dany",
                                new AuthenticationState.Authenticated("dany", true)
                        ),
                        "Hi dany!"
                ),
                Arguments.of(
                        Named.named(
                                "an authenticated admin called root",
                                new AuthenticationState.Authenticated("root", true)
                        ),
                        "Greetings grand master!"
                ),
                Arguments.of(
                        Named.named(
                                "a failed attempt to log in with error code 401",
                                new AuthenticationState.AuthError(401)
                        ),
                        "Oops, couldn't log you in (reason: bad credentials)."
                ),
                Arguments.of(
                        Named.named(
                                "a failed attempt to log in with error code 403",
                                new AuthenticationState.AuthError(403)
                        ),
                        "Sorry, your account has been disabled."
                ),
                Arguments.of(
                        Named.named(
                                "a failed attempt to log in with error code 404",
                                new AuthenticationState.AuthError(404)
                        ),
                        "Sorry, this account has been deleted or doesn't exist."
                ),
                Arguments.of(
                        Named.named(
                                "a failed attempt to log in with error code 503",
                                new AuthenticationState.AuthError(503)
                        ),
                        "Impossible to connect to the authentication server."
                ),
                Arguments.of(
                        Named.named(
                                "a failed attempt to log in any with error code",
                                new AuthenticationState.AuthError(666)
                        ),
                        "An unknown error happened."
                )
        );
    }

    @DisplayName("Greeting from an authentication state:")
    @ParameterizedTest(name = "should be ''{1}'' when state corresponds to {0}")
    @MethodSource("createAuthenticationStatesForTesting")
    void getGreetingFromAuthenticationState(AuthenticationState state, String expected) {
        // Given a greeter
        Greeter greeter = new Greeter();

        // When passing an state to the greeting function
        String result = greeter.getGreetingFromAuthenticationState(state);

        // Then the result should be the expected one
        assertEquals(expected, result);
    }
}
