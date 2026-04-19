package pl.edu.healthapp.mapper;

import pl.edu.healthapp.dto.request.SleepCreationDTO;
import pl.edu.healthapp.dto.response.SleepDTO;
import pl.edu.healthapp.model.SleepEntry;
import pl.edu.healthapp.model.SleepQuality;

import java.time.OffsetDateTime;

public class SleepMapper {

    public static SleepEntry fromJSON(String json) {
        String clean = json.replace("{", "").replace("}", "").replace("\"", "");

        String[] pairs = clean.split(",");

        SleepEntry sleep = new SleepEntry();

        for (String pair : pairs) {
            String[] keyValue = pair.split(":", 2);
            String key = keyValue[0].trim();
            String value = keyValue[1].trim();

            if (key.equals("start"))
                sleep.setSleepStart(OffsetDateTime.parse(value));
            else if (key.equals("end"))
                sleep.setSleepEnd(OffsetDateTime.parse(value));
            else if (key.equals("quality"))
                sleep.setQuality(SleepQuality.valueOf(value));
        }

        return sleep;
    }

    public static String toJSON(SleepEntry sleep){
        return "\"start\": \"%s\",\n\"end\": \"%s\",\n\"quality\": \"%s\"".formatted(sleep.getSleepStart().toString(), sleep.getSleepEnd().toString(), sleep.getQuality().name());
    }

    public static SleepDTO fromEntity(SleepEntry sleep){
        return new SleepDTO(
                            sleep.getId(),
                            sleep.getSleepStart(),
                            sleep.getSleepEnd(),
                            sleep.getQuality()
        );
    }

    public static SleepEntry toEntity(SleepCreationDTO sleep){
        return SleepEntry.builder()
                .sleepStart(sleep.start())
                .sleepEnd(sleep.end())
                .quality(sleep.quality())
                .build();
    }
}
