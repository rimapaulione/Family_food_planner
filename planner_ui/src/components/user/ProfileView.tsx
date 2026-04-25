import {Pencil} from 'lucide-react';
import {Avatar} from '@/components/ui/Avatar';
import {Button} from '@/components/ui/Button';

interface ProfileViewProps {
    displayName: string;
    email: string;
    avatarUrl: string | null;
    onEdit: () => void;
}

export function ProfileView({displayName, email, avatarUrl, onEdit}: ProfileViewProps) {
    return (
        <section className="flex items-center gap-4">
            <Avatar src={avatarUrl} size="lg"/>
            <div className="min-w-0 flex-1">
                <p className="font-medium text-foreground">{displayName}</p>
                <p className="truncate text-sm text-muted-foreground">{email}</p>
            </div>
            <Button variant="outline" icon={Pencil} onClick={onEdit}>
                Edit
            </Button>
        </section>
    );
}
