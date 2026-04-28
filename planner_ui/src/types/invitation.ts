export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'EXPIRED' | 'CANCELLED';

export interface Invitation {
    id: string;
    invitedEmail: string;
    status: InvitationStatus;
    token: string;
    createdAt: string;
    expiresAt: string;
}

export interface InvitationCreateRequest {
    email: string;
}
