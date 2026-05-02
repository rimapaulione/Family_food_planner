package org.example.planner_backend.service;

import org.example.planner_backend.dto.mealplan.MealPlanUpdateRequestDto;
import org.example.planner_backend.dto.mealplan.MealSlotUpdateRequestDto;
import org.example.planner_backend.exception.ConflictException;
import org.example.planner_backend.exception.ResourceNotFoundException;
import org.example.planner_backend.exception.UnauthorizedException;
import org.example.planner_backend.mapper.MealPlanMapper;
import org.example.planner_backend.model.entity.AppUser;
import org.example.planner_backend.model.entity.Family;
import org.example.planner_backend.model.entity.MealPlan;
import org.example.planner_backend.model.entity.MealServings;
import org.example.planner_backend.model.entity.MealSlot;
import org.example.planner_backend.model.entity.Recipe;
import org.example.planner_backend.model.enums.AuthProvider;
import org.example.planner_backend.model.enums.MealType;
import org.example.planner_backend.model.enums.PlanStatus;
import org.example.planner_backend.model.enums.Role;
import org.example.planner_backend.repository.MealPlanRepository;
import org.example.planner_backend.repository.MealSlotRepository;
import org.example.planner_backend.repository.RecipeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MealPlanServiceTest {

    private static final UUID USER_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID FAMILY_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID OTHER_FAMILY_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");
    private static final UUID PLAN_ID = UUID.fromString("44444444-4444-4444-4444-444444444444");
    private static final UUID SLOT_ID = UUID.fromString("55555555-5555-5555-5555-555555555555");
    private static final UUID RECIPE_ID = UUID.fromString("66666666-6666-6666-6666-666666666666");
    private static final String EMAIL = "user@example.com";

    @Mock
    private MealPlanRepository mealPlanRepository;
    @Mock
    private MealSlotRepository mealSlotRepository;
    @Mock
    private RecipeRepository recipeRepository;
    @Mock
    private FamilyResolver familyResolver;
    @Mock
    private MealPlanMapper mealPlanMapper;

    @InjectMocks
    private MealPlanService mealPlanService;

    private AppUser admin;
    private Family family;
    private Family otherFamily;
    private Recipe recipe;

    @BeforeEach
    void setUp() {
        family = Family.builder()
                .id(FAMILY_ID)
                .name("Smith")
                .shoppingDay(DayOfWeek.SUNDAY)
                .defaultWeekdayServings(new MealServings(3, null, 4))
                .defaultWeekendServings(new MealServings(4, 4, 4))
                .build();
        otherFamily = Family.builder().id(OTHER_FAMILY_ID).name("Other").build();
        admin = AppUser.builder()
                .id(USER_ID)
                .email(EMAIL)
                .displayName("Admin")
                .authProvider(AuthProvider.LOCAL)
                .role(Role.ADMIN)
                .family(family)
                .build();
        recipe = Recipe.builder().id(RECIPE_ID).name("Pasta").family(family).build();
    }

    // ---------- getCurrentAndNext ----------

    @Test
    void getCurrentAndNext_shouldCreateBothPlansWhenMissing() {
        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(mealPlanRepository.findByFamilyIdAndStartDate(any(), any())).thenReturn(Optional.empty());
        when(mealPlanRepository.save(any(MealPlan.class))).thenAnswer(i -> {
            MealPlan p = i.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });
        when(mealSlotRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        mealPlanService.getCurrentAndNext(EMAIL);

        verify(mealPlanRepository, times(2)).save(any(MealPlan.class));
        verify(mealSlotRepository, times(2)).saveAll(any());
    }

    @Test
    void getCurrentAndNext_shouldReturnExistingPlansWithoutCreating() {
        MealPlan existing = MealPlan.builder().id(PLAN_ID).family(family).status(PlanStatus.DRAFT).build();
        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(mealPlanRepository.findByFamilyIdAndStartDate(any(), any())).thenReturn(Optional.of(existing));

        mealPlanService.getCurrentAndNext(EMAIL);

        verify(mealPlanRepository, never()).save(any());
        verify(mealSlotRepository, never()).saveAll(any());
    }

    @Test
    @SuppressWarnings("unchecked")
    void getCurrentAndNext_shouldGenerate21SlotsPerWeek() {
        ArgumentCaptor<List<MealSlot>> captor = ArgumentCaptor.forClass(List.class);
        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(mealPlanRepository.findByFamilyIdAndStartDate(any(), any())).thenReturn(Optional.empty());
        when(mealPlanRepository.save(any(MealPlan.class))).thenAnswer(i -> {
            MealPlan p = i.getArgument(0);
            p.setId(UUID.randomUUID());
            return p;
        });
        when(mealSlotRepository.saveAll(any())).thenAnswer(i -> i.getArgument(0));

        mealPlanService.getCurrentAndNext(EMAIL);

        verify(mealSlotRepository, times(2)).saveAll(captor.capture());
        List<MealSlot> firstWeekSlots = captor.getAllValues().get(0);
        // 7 days × 3 meal types = 21 slots, regardless of family settings
        assertThat(firstWeekSlots).hasSize(21);
        assertThat(firstWeekSlots.stream().filter(s -> s.getMealType() == MealType.BREAKFAST).count())
                .isEqualTo(7);
        assertThat(firstWeekSlots.stream().filter(s -> s.getMealType() == MealType.LUNCH).count())
                .isEqualTo(7);
        assertThat(firstWeekSlots.stream().filter(s -> s.getMealType() == MealType.DINNER).count())
                .isEqualTo(7);
    }


    // ---------- updateSlot ----------

    @Test
    void updateSlot_shouldAssignRecipe() {
        MealPlan plan = MealPlan.builder().id(PLAN_ID).family(family).status(PlanStatus.DRAFT).build();
        MealSlot slot = MealSlot.builder()
                .id(SLOT_ID).mealPlan(plan)
                .date(LocalDate.now()).mealType(MealType.DINNER)
                .build();
        MealSlotUpdateRequestDto request = new MealSlotUpdateRequestDto(RECIPE_ID, 4);

        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(mealSlotRepository.findById(SLOT_ID)).thenReturn(Optional.of(slot));
        when(recipeRepository.findByIdAndFamilyId(RECIPE_ID, FAMILY_ID)).thenReturn(Optional.of(recipe));

        mealPlanService.updateSlot(EMAIL, SLOT_ID, request);

        assertThat(slot.getRecipe()).isEqualTo(recipe);
        assertThat(slot.getServings()).isEqualTo(4);
    }

    @Test
    void updateSlot_shouldClearRecipeWhenIdIsNull() {
        MealPlan plan = MealPlan.builder().id(PLAN_ID).family(family).status(PlanStatus.DRAFT).build();
        MealSlot slot = MealSlot.builder()
                .id(SLOT_ID).mealPlan(plan)
                .date(LocalDate.now()).mealType(MealType.DINNER)
                .recipe(recipe).servings(4)
                .build();
        MealSlotUpdateRequestDto request = new MealSlotUpdateRequestDto(null, null);

        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(mealSlotRepository.findById(SLOT_ID)).thenReturn(Optional.of(slot));

        mealPlanService.updateSlot(EMAIL, SLOT_ID, request);

        assertThat(slot.getRecipe()).isNull();
        assertThat(slot.getServings()).isNull();
    }

    @Test
    void updateSlot_shouldThrowNotFoundWhenSlotMissing() {
        MealSlotUpdateRequestDto request = new MealSlotUpdateRequestDto(RECIPE_ID, 4);
        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(mealSlotRepository.findById(SLOT_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mealPlanService.updateSlot(EMAIL, SLOT_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updateSlot_shouldThrowUnauthorizedWhenSlotInDifferentFamily() {
        MealPlan plan = MealPlan.builder().id(PLAN_ID).family(otherFamily).status(PlanStatus.DRAFT).build();
        MealSlot slot = MealSlot.builder()
                .id(SLOT_ID).mealPlan(plan)
                .date(LocalDate.now()).mealType(MealType.DINNER)
                .build();
        MealSlotUpdateRequestDto request = new MealSlotUpdateRequestDto(RECIPE_ID, 4);

        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(mealSlotRepository.findById(SLOT_ID)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> mealPlanService.updateSlot(EMAIL, SLOT_ID, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not in your family");
    }

    @Test
    void updateSlot_shouldThrowConflictWhenPlanLocked() {
        MealPlan plan = MealPlan.builder().id(PLAN_ID).family(family).status(PlanStatus.LOCKED).build();
        MealSlot slot = MealSlot.builder()
                .id(SLOT_ID).mealPlan(plan)
                .date(LocalDate.now()).mealType(MealType.DINNER)
                .build();
        MealSlotUpdateRequestDto request = new MealSlotUpdateRequestDto(RECIPE_ID, 4);

        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(mealSlotRepository.findById(SLOT_ID)).thenReturn(Optional.of(slot));

        assertThatThrownBy(() -> mealPlanService.updateSlot(EMAIL, SLOT_ID, request))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("locked");
    }

    @Test
    void updateSlot_shouldThrowUnauthorizedWhenRecipeInDifferentFamily() {
        MealPlan plan = MealPlan.builder().id(PLAN_ID).family(family).status(PlanStatus.DRAFT).build();
        MealSlot slot = MealSlot.builder()
                .id(SLOT_ID).mealPlan(plan)
                .date(LocalDate.now()).mealType(MealType.DINNER)
                .build();
        MealSlotUpdateRequestDto request = new MealSlotUpdateRequestDto(RECIPE_ID, 4);

        when(familyResolver.getFamilyByEmail(EMAIL)).thenReturn(family);
        when(mealSlotRepository.findById(SLOT_ID)).thenReturn(Optional.of(slot));
        when(recipeRepository.findByIdAndFamilyId(RECIPE_ID, FAMILY_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mealPlanService.updateSlot(EMAIL, SLOT_ID, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Recipe not in your family");
    }

    // ---------- updatePlan ----------

    @Test
    void updatePlan_shouldLockDraftPlan() {
        MealPlan plan = MealPlan.builder().id(PLAN_ID).family(family).status(PlanStatus.DRAFT).build();
        MealPlanUpdateRequestDto request = new MealPlanUpdateRequestDto(PlanStatus.LOCKED);

        when(mealPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));
        when(familyResolver.getAdminUser(EMAIL)).thenReturn(admin);

        mealPlanService.updatePlan(EMAIL, PLAN_ID, request);

        assertThat(plan.getStatus()).isEqualTo(PlanStatus.LOCKED);
    }

    @Test
    void updatePlan_shouldUnlockLockedPlan() {
        MealPlan plan = MealPlan.builder().id(PLAN_ID).family(family).status(PlanStatus.LOCKED).build();
        MealPlanUpdateRequestDto request = new MealPlanUpdateRequestDto(PlanStatus.DRAFT);

        when(mealPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));
        when(familyResolver.getAdminUser(EMAIL)).thenReturn(admin);

        mealPlanService.updatePlan(EMAIL, PLAN_ID, request);

        assertThat(plan.getStatus()).isEqualTo(PlanStatus.DRAFT);
    }

    @Test
    void updatePlan_shouldThrowUnauthorizedWhenNotAdmin() {
        MealPlan plan = MealPlan.builder().id(PLAN_ID).family(family).status(PlanStatus.DRAFT).build();
        MealPlanUpdateRequestDto request = new MealPlanUpdateRequestDto(PlanStatus.LOCKED);

        when(mealPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));
        when(familyResolver.getAdminUser(EMAIL))
                .thenThrow(new UnauthorizedException("Admin role required"));

        assertThatThrownBy(() -> mealPlanService.updatePlan(EMAIL, PLAN_ID, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("Admin");

        assertThat(plan.getStatus()).isEqualTo(PlanStatus.DRAFT);
    }

    @Test
    void updatePlan_shouldThrowUnauthorizedWhenPlanInDifferentFamily() {
        MealPlan plan = MealPlan.builder().id(PLAN_ID).family(otherFamily).status(PlanStatus.DRAFT).build();
        MealPlanUpdateRequestDto request = new MealPlanUpdateRequestDto(PlanStatus.LOCKED);

        when(mealPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));
        when(familyResolver.getAdminUser(EMAIL)).thenReturn(admin);

        assertThatThrownBy(() -> mealPlanService.updatePlan(EMAIL, PLAN_ID, request))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessageContaining("not in your family");

        assertThat(plan.getStatus()).isEqualTo(PlanStatus.DRAFT);
    }

    @Test
    void updatePlan_shouldThrowNotFoundWhenPlanMissing() {
        MealPlanUpdateRequestDto request = new MealPlanUpdateRequestDto(PlanStatus.LOCKED);
        when(mealPlanRepository.findById(PLAN_ID)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> mealPlanService.updatePlan(EMAIL, PLAN_ID, request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("not found");
    }

    @Test
    void updatePlan_shouldBeIdempotentWhenLockingAlreadyLockedPlan() {
        MealPlan plan = MealPlan.builder().id(PLAN_ID).family(family).status(PlanStatus.LOCKED).build();
        MealPlanUpdateRequestDto request = new MealPlanUpdateRequestDto(PlanStatus.LOCKED);

        when(mealPlanRepository.findById(PLAN_ID)).thenReturn(Optional.of(plan));
        when(familyResolver.getAdminUser(EMAIL)).thenReturn(admin);

        mealPlanService.updatePlan(EMAIL, PLAN_ID, request);

        assertThat(plan.getStatus()).isEqualTo(PlanStatus.LOCKED);
    }
}
