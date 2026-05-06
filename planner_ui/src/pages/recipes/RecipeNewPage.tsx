import {useState} from 'react';
import {useNavigate} from 'react-router-dom';
import {Sparkles} from 'lucide-react';
import {useCreateRecipe} from '@/hooks/useRecipes';
import type {AiRecipeGeneration, RecipeRequest} from '@/types/recipe';
import {BackLink} from '@/components/ui/BackLink';
import {Button} from '@/components/ui/Button';
import {RecipeForm} from '@/components/recipes/RecipeForm';
import {AiGenerateModal} from '@/components/recipes/AiGenerateModal';

export function RecipeNewPage() {
    const navigate = useNavigate();
    const createMutation = useCreateRecipe();
    const [aiModalOpen, setAiModalOpen] = useState(false);
    const [aiData, setAiData] = useState<AiRecipeGeneration | null>(null);

    const handleSubmit = async (data: RecipeRequest) => {
        await createMutation.mutateAsync(data);
        navigate('/recipes');
    };

    return (
        <div className="mx-auto max-w-2xl space-y-6">
            <BackLink to="/recipes"/>
            <div className="flex items-center justify-between gap-2">
                <h1 className="text-2xl font-bold text-foreground">New Recipe</h1>
                <Button variant="primary" icon={Sparkles} onClick={() => setAiModalOpen(true)}>
                    <span className="sm:hidden">AI</span>
                    <span className="hidden sm:inline">Generate with AI</span>
                </Button>
            </div>
            <RecipeForm
                aiData={aiData}
                onSubmit={handleSubmit}
                onCancel={() => navigate('/recipes')}
                isPending={createMutation.isPending}
                submitLabel="Create"
            />

            {aiModalOpen && (
                <AiGenerateModal
                    onClose={() => setAiModalOpen(false)}
                    onGenerated={setAiData}
                />
            )}
        </div>
    );
}
