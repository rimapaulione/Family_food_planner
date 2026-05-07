import {useState, type FormEvent} from 'react';
import {Sparkles, X} from 'lucide-react';
import {useGenerateRecipe} from '@/hooks/useAiRecipe';
import {Button} from '@/components/ui/Button';
import type {AiRecipeGeneration} from '@/types/recipe';

interface AiGenerateModalProps {
    onClose: () => void;
    onGenerated: (result: AiRecipeGeneration) => void;
}

export function AiGenerateModal({onClose, onGenerated}: AiGenerateModalProps) {
    const [prompt, setPrompt] = useState('');
    const generateMutation = useGenerateRecipe();

    const handleSubmit = (e: FormEvent) => {
        e.preventDefault();
        const trimmed = prompt.trim();
        if (!trimmed) return;
        generateMutation.mutate(trimmed, {
            onSuccess: (result) => {
                onGenerated(result);
                onClose();
            },
        });
    };

    return (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/50 px-4" onClick={onClose}>
            <div
                className="w-full max-w-md rounded-lg border border-border bg-card p-4 shadow-lg"
                onClick={(e) => e.stopPropagation()}
            >
                <div className="mb-3 flex items-center justify-between">
                    <h3 className="flex items-center gap-2 font-semibold">
                        <Sparkles className="h-4 w-4"/> Generate recipe with AI
                    </h3>
                    <button onClick={onClose} className="rounded p-1 text-muted-foreground hover:bg-accent">
                        <X className="h-4 w-4"/>
                    </button>
                </div>

                <form onSubmit={handleSubmit} className="space-y-3">
                    <textarea
                        value={prompt}
                        onChange={(e) => setPrompt(e.target.value)}
                        placeholder="e.g., Tomato soup with rice for a family of 4"
                        rows={4}
                        autoFocus
                        maxLength={500}
                        className="w-full rounded-md border border-input bg-background px-3 py-2 text-base md:text-sm outline-none focus:ring-2 focus:ring-ring"
                    />
                    <div className="flex justify-end gap-2">
                        <Button variant="outline" onClick={onClose}>
                            Cancel
                        </Button>
                        <Button
                            variant="primary"
                            icon={Sparkles}
                            type="submit"
                            disabled={generateMutation.isPending || prompt.trim().length === 0}
                        >
                            {generateMutation.isPending ? 'Generating...' : 'Generate'}
                        </Button>
                    </div>
                </form>
            </div>
        </div>
    );
}
