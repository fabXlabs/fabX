export interface Error {
    type: string,
    message: string,
    parameters: Record<string, string>,
    correlationId: string | null
}
