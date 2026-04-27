import type {ReactNode} from 'react';
import {Link} from 'react-router-dom';
import {CenteredCard} from '@/components/ui/CenteredCard';

interface AuthCardProps {
    title: string;
    subtitle: string;
    children: ReactNode;
    footerPrompt: string;
    footerLinkText: string;
    footerLinkTo: string;
}

export function AuthCard({
    title,
    subtitle,
    children,
    footerPrompt,
    footerLinkText,
    footerLinkTo,
}: AuthCardProps) {
    return (
        <CenteredCard
            title={title}
            subtitle={subtitle}
            footer={
                <>
                    {footerPrompt}{' '}
                    <Link to={footerLinkTo} className="font-medium text-primary hover:underline">
                        {footerLinkText}
                    </Link>
                </>
            }
        >
            {children}
        </CenteredCard>
    );
}
