package br.edu.utfpr.allansantos.teste;

import androidx.room.TypeConverter;
import java.time.LocalDate;

public class Converters {
    @TypeConverter
    public static Long fromLocalDate(LocalDate date) {
        return date == null ? null : date.toEpochDay();
    }

    @TypeConverter
    public static LocalDate toLocalDate(Long epochDay) {
        return epochDay == null ? null : LocalDate.ofEpochDay(epochDay);
    }
}
