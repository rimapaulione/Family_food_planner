export type InvitationStatus = 'PENDING' | 'ACCEPTED' | 'EXPIRED' | 'CANCELLED';

export interface Invitation {
    id: string;
    invitedEmail: string;
    status: InvitationStatus;
    token: string;
    createdAt: string;
    expiresAt: string;
}

export interface InvitationPublic {
    familyName: string;
    invitedEmail: string;
    status: InvitationStatus;
}

export interface InvitationCreateRequest {
    email: string;
}
