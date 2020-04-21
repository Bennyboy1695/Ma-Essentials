package com.maciej916.maessentials;

import com.google.common.base.Utf8;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import net.kyori.text.Component;
import net.kyori.text.TextComponent;
import net.kyori.text.serializer.gson.GsonComponentSerializer;
import net.kyori.text.serializer.legacy.LegacyComponentSerializer;
import net.minecraft.util.text.ITextComponent;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class TextUtils {

    public static ITextComponent convertToITextComponent(Component component) {
        return ITextComponent.Serializer.fromJson(GsonComponentSerializer.INSTANCE.serialize(component));
    }

    public static ITextComponent translateFromJson(String translate, Object... args) {
        try (InputStream inputStream = MaEssentials.class.getResourceAsStream("/assets/maessentials/lang/en_us.json")) {
            Reader reader = new InputStreamReader(inputStream, StandardCharsets.UTF_8);
            JsonObject object = new Gson().fromJson(reader, JsonObject.class);
            String translationKey = object.get(translate).getAsString();
            Object[] formattedArgs = new Object[args.length];
            for (int i = 0; i < args.length; i++) {
                if (args[i] instanceof ITextComponent) {
                    formattedArgs[i] = GsonComponentSerializer.INSTANCE.deserialize(ITextComponent.Serializer.toJson((ITextComponent) args[i]));
                } else {
                    formattedArgs[i] = args[i];
                }
            }
            TextComponent.Builder builder = TextComponent.builder().append(LegacyComponentSerializer.legacy().deserialize(String.format(translationKey, formattedArgs).replace("§", "&"), '&'));
            return convertToITextComponent(builder.build());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
