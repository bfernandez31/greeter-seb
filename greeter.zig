const std = @import("std");
const testing = std.testing;
const expect = testing.expect;
const expectEqualStrings = testing.expectEqualStrings;

pub const Role = enum {
    user,
    admin,
};

pub const AuthError = enum {
    bad_credentials,
    account_disabled,
    account_not_found,
    server_error,
    unknown,

    pub fn fromCode(code: u16) AuthError {
        return switch (code) {
            401 => .bad_credentials,
            403 => .account_disabled,
            404 => .account_not_found,
            503 => .server_error,
            else => .unknown,
        };
    }
};

pub const AuthenticationState = union(enum) {
    anonymous: void,
    authenticated: struct {
        username: []const u8,
        role: Role,
    },
    erreur: AuthError,
};

pub const Greeter = struct {
    allocator: std.mem.Allocator,

    pub fn init(allocator: std.mem.Allocator) Greeter {
        return .{ .allocator = allocator };
    }

    pub fn getGreetingFromAuthenticationState(self: *const Greeter, state: AuthenticationState) ![]u8 {
        return switch (state) {
            .anonymous => try self.allocator.dupe(u8, "Welcome guest!"),
            .authenticated => |auth| {
                if (auth.role == .admin and std.mem.eql(u8, auth.username, "root")) {
                    return try self.allocator.dupe(u8, "Greetings grand master!");
                }
                const prefix = if (auth.role == .admin) "Hi " else "Hello ";
                return try std.fmt.allocPrint(
                    self.allocator,
                    "{s}{s}!",
                    .{ prefix, auth.username },
                );
            },
            .erreur => |err| try self.allocator.dupe(u8, switch (err) {
                .bad_credentials => "Oops, couldn't log you in (reason: bad credentials).",
                .account_disabled => "Sorry, your account has been disabled.",
                .account_not_found => "Sorry, this account has been deleted or doesn't exist.",
                .server_error => "Impossible to connect to the authentication server.",
                .unknown => "An unknown error happened.",
            }),
        };
    }
};

test "Greeter test cases" {
    const allocator = testing.allocator;
    var greeter = Greeter.init(allocator);

    const TestCase = struct {
        name: []const u8,
        state: AuthenticationState,
        expected: []const u8,
    };

    const test_cases = [_]TestCase{
        .{
            .name = "anonymous user",
            .state = .anonymous,
            .expected = "Welcome guest!",
        },
        .{
            .name = "authenticated user alice",
            .state = .{ .authenticated = .{
                .username = "alice",
                .role = .user,
            } },
            .expected = "Hello alice!",
        },
        .{
            .name = "authenticated admin dany",
            .state = .{ .authenticated = .{
                .username = "dany",
                .role = .admin,
            } },
            .expected = "Hi dany!",
        },
        .{
            .name = "authenticated admin root",
            .state = .{ .authenticated = .{
                .username = "root",
                .role = .admin,
            } },
            .expected = "Greetings grand master!",
        },
        .{
            .name = "error 401",
            .state = .{ .erreur = AuthError.fromCode(401) },
            .expected = "Oops, couldn't log you in (reason: bad credentials).",
        },
        .{
            .name = "error 403",
            .state = .{ .erreur = AuthError.fromCode(403) },
            .expected = "Sorry, your account has been disabled.",
        },
        .{
            .name = "error 404",
            .state = .{ .erreur = AuthError.fromCode(404) },
            .expected = "Sorry, this account has been deleted or doesn't exist.",
        },
        .{
            .name = "error 503",
            .state = .{ .erreur = AuthError.fromCode(503) },
            .expected = "Impossible to connect to the authentication server.",
        },
        .{
            .name = "unknown error",
            .state = .{ .erreur = AuthError.fromCode(666) },
            .expected = "An unknown error happened.",
        },
    };

    for (test_cases) |test_case| {
        const result = try greeter.getGreetingFromAuthenticationState(test_case.state);
        defer allocator.free(result);
        try expectEqualStrings(test_case.expected, result);
    }
}
