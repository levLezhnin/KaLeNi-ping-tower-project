import api from "./apiClient";

function arrayBufferToBase64(buffer: ArrayBuffer): Promise<string> {
    return new Promise((resolve) => {
        const blob = new Blob([buffer], { type: 'image/png' });
        const reader = new FileReader();
        reader.onloadend = () => {
            const dataUrl = reader.result as string;
            // data:image/png;base64,...
            resolve(dataUrl.split(",")[1]);
        };
        reader.readAsDataURL(blob);
    });
}

export const telegramService = {
    async getSubscribeLink(userId: string | number): Promise<string> {
        const { data } = await api.get(`/api/v1/telegramNotifications/getSubscribeLink`, {
            data: userId,
            headers: { 'Content-Type': 'application/json' },
        });
        return data;
    },
    async getSubscribeQrCode(userId: string | number): Promise<string> {
        const { data } = await api.get(`/api/v1/telegramNotifications/getSubscribeQrCode`, {
            data: userId,
            headers: { 'Content-Type': 'application/json' },
            responseType: "arraybuffer",
        });
        return await arrayBufferToBase64(data);
    },
};
