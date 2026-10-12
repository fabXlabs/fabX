import { defineEnvVars } from '@sveltejs/kit/env';

export const variables = defineEnvVars({ PUBLIC_FABX_VERSION: { public: true, static: true } });
