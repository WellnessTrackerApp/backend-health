package pl.edu.healthapp.mapper;

import pl.edu.healthapp.dto.request.DietCreationDTO;
import pl.edu.healthapp.dto.response.DietDTO;
import pl.edu.healthapp.model.DietEntry;
import pl.edu.healthapp.model.Macronutrients;

import java.time.OffsetDateTime;

public class DietMapper {

    public static DietEntry fromJSON(String json) {
        String clean = json.replace("{", "").replace("}", "").replace("\"", "");

        String[] pairs = clean.split(",");

        DietEntry meal = new DietEntry();
        int calories = 0;
        double protein = 0;
        double carbs = 0;
        double fat = 0;

        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            if (key.equals("mealName"))
                meal.setMealName(value);
            else if (key.equals("eatenAt"))
                meal.setEatenAt(OffsetDateTime.parse(value));
            else if (key.equals("calories"))
                calories = Integer.parseInt(value);
            else if (key.equals("protein"))
                protein = Double.parseDouble(value);
            else if (key.equals("carbs"))
                carbs = Double.parseDouble(value);
            else if (key.equals("fat"))
                fat = Double.parseDouble(value);
        }

        Macronutrients macro = new Macronutrients(calories, protein, carbs, fat);
        meal.setMacronutrients(macro);

        return meal;
    }

    public static String toJSON(DietEntry diet){
        return "\"mealName\": \"%s\",\n\"eatenAt\": \"%s\",\n\"calories\": \"%d\",\n\"protein\": \"%.1f\",\n\"carbs\": \"%.1f\",\n\"fat\": \"%.1f\"".formatted(diet.getMealName(), diet.getEatenAt().toString(), diet.getMacronutrients().calories(), diet.getMacronutrients().protein(), diet.getMacronutrients().carbs(), diet.getMacronutrients().fat());
    }

    public static DietDTO fromEntity(DietEntry diet){
        return new DietDTO(
                            diet.getId(),
                            diet.getMealName(),
                            diet.getMacronutrients().calories(),
                            diet.getMacronutrients().protein(),
                            diet.getMacronutrients().carbs(),
                            diet.getMacronutrients().fat(),
                            diet.getEatenAt()
        );
    }

    public static DietEntry toEntity(DietCreationDTO diet){
        return DietEntry.builder()
                .mealName(diet.mealName())
                .macronutrients(new Macronutrients( diet.calories(),
                                                    diet.protein(),
                                                    diet.carbs(),
                                                    diet.fat()))
                .eatenAt(diet.eatenAt())
                .build();
    }
}
