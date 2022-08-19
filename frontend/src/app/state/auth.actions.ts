export namespace Auth {
    export class Login {
        static readonly type = '[Auth] Login';

        constructor(public payload: { username: string; password: string }) {}
    }

    export class LoginWebauthn {
        static readonly type = '[Auth] Login Webauthn';

        constructor(public username: string) {}
    }

    export class Logout {
        static readonly type = '[Auth] Logout';
    }
}
