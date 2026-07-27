export class Guid {
    // crypto.randomUUID() requires a secure context (HTTPS/localhost); the app is also served
    // over plain HTTP between containers in the E2E test environment, so use the
    // context-independent crypto.getRandomValues() to build an RFC 4122 v4 UUID instead.
    static newGuid() {
        const bytes = crypto.getRandomValues(new Uint8Array(16));
        bytes[6] = (bytes[6] & 0x0f) | 0x40;
        bytes[8] = (bytes[8] & 0x3f) | 0x80;
        const hex = Array.from(bytes, b => b.toString(16).padStart(2, '0')).join('');
        return `${hex.slice(0, 8)}-${hex.slice(8, 12)}-${hex.slice(12, 16)}-${hex.slice(16, 20)}-${hex.slice(20)}`;
    }
}
