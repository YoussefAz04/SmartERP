interface SmartErpRuntimeConfig {
  apiUrl?: string;
}

type SmartErpGlobal = typeof globalThis & {
  __SMARTERP_CONFIG__?: SmartErpRuntimeConfig;
};

export function runtimeApiUrl(defaultUrl: string): string {
  const configuredUrl = (globalThis as SmartErpGlobal).__SMARTERP_CONFIG__?.apiUrl?.trim();
  return configuredUrl ? configuredUrl.replace(/\/$/, '') : defaultUrl;
}
