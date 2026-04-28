import {KeyRound} from 'lucide-react';
import {Button} from '@/components/ui/Button';
import {Card} from '@/components/ui/Card';

interface PasswordViewProps {
    onChange: () => void;
}

export function PasswordView({onChange}: PasswordViewProps) {
    return (
        <Card padding="md">
            <div className="flex items-center">
                <h2 className="text-lg font-semibold text-foreground">Password</h2>
                <Button variant="outline" icon={KeyRound} onClick={onChange} className="ml-auto">
                    Change
                </Button>
            </div>
        </Card>
    );
}
