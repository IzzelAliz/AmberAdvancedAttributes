package io.izzel.aaa.command;

import com.google.common.collect.ImmutableList;
import com.google.inject.Inject;
import io.izzel.aaa.AmberAdvancedAttributes;
import io.izzel.aaa.byteitems.ByteItemsHandler;
import io.izzel.aaa.data.StringValue;
import io.izzel.aaa.service.Attributes;
import io.izzel.aaa.util.DataUtil;
import io.izzel.amber.commons.i18n.AmberLocale;
import org.apache.commons.lang3.StringUtils;
import org.spongepowered.api.command.CommandCallable;
import org.spongepowered.api.command.CommandResult;
import org.spongepowered.api.command.args.GenericArguments;
import org.spongepowered.api.command.spec.CommandSpec;
import org.spongepowered.api.data.key.Keys;
import org.spongepowered.api.data.type.HandTypes;
import org.spongepowered.api.entity.living.player.Player;
import org.spongepowered.api.item.inventory.ItemStack;
import org.spongepowered.api.item.inventory.ItemStackSnapshot;
import org.spongepowered.api.item.inventory.transaction.InventoryTransactionResult;
import org.spongepowered.api.text.Text;
import org.spongepowered.api.text.serializer.TextSerializers;

import java.util.Optional;
import java.util.regex.Pattern;

class ItemCommand {

    private static final Pattern NAME_PATTERN = Pattern.compile("[a-z0-9]+(?:[-_][a-z0-9]+)*");

    @Inject private AmberLocale locale;
    @Inject private ByteItemsHandler biHandler;

    CommandCallable callable() {
        return CommandSpec.builder()
                .permission(AmberAdvancedAttributes.ID + ".command.aaa-items")
                .arguments(GenericArguments.firstParsing(
                        GenericArguments.literal(Text.of("give"), "give"),
                        GenericArguments.literal(Text.of("save"), "save")),
                        GenericArguments.string(Text.of("name")))
                .executor((src, args) -> {
                    String name = args.<String>getOne(Text.of("name")).orElse("null");
                    if (!NAME_PATTERN.matcher(name).matches()) {
                        this.locale.to(src, "commands.byte-items.invalid-name", name);
                        return CommandResult.success();
                    }
                    if (args.hasAny(Text.of("save"))) {
                        if (src instanceof Player) {
                            Player player = (Player) src;
                            Optional<ItemStack> stackOptional = player.getItemInHand(HandTypes.MAIN_HAND);
                            if (stackOptional.isPresent()) {
                                ItemStack stack = stackOptional.get();
                                if (DataUtil.hasData(stack)) {
                                    stack.remove(Keys.ITEM_LORE); // lore texts are generated
                                    Attributes.ID.setValues(stack, ImmutableList.of(StringValue.of(name)));
                                    player.setItemInHand(HandTypes.MAIN_HAND, stack);
                                    this.biHandler.save(name, player);
                                    this.locale.to(src, "commands.byte-items.save-succeed", name, "aaa-" + name);
                                    return CommandResult.success();
                                }
                            }
                        }
                        this.locale.to(src, "commands.drop.nonexist");
                    }
                    if (args.hasAny(Text.of("give"))) {
                        if (src instanceof Player) {
                            Player player = (Player) src;
                            ItemStackSnapshot snapshot = this.biHandler.read(name);
                            if (snapshot.isEmpty()) {
                                this.locale.to(src, "commands.byte-items.nonexist", name, "aaa-" + name);
                                return CommandResult.success();
                            }
                            InventoryTransactionResult result = player.getInventory().offer(snapshot.createStack());
                            if (InventoryTransactionResult.Type.SUCCESS.equals(result.getType())) {
                                this.locale.to(src, "commands.byte-items.give-succeed", name, "aaa-" + name);
                                return CommandResult.success();
                            }
                        }
                        this.locale.to(src, "commands.byte-items.full");
                    }
                    return CommandResult.success();
                })
                .child(CommandSpec.builder()
                        .arguments(GenericArguments.optionalWeak(GenericArguments.remainingJoinedStrings(Text.of("display"))))
                        .executor((src, args) -> {
                            if (src instanceof Player) {
                                Optional<ItemStack> optional = ((Player) src).getItemInHand(HandTypes.MAIN_HAND);
                                if (optional.isPresent()) {
                                    ItemStack stack = optional.get();
                                    String text = args.<String>getOne("display").orElse(null);
                                    if (StringUtils.isNotEmpty(text)) {
                                        stack.offer(Keys.DISPLAY_NAME, TextSerializers.FORMATTING_CODE.deserialize(text));
                                    } else {
                                        stack.remove(Keys.DISPLAY_NAME);
                                    }
                                    return CommandResult.success();
                                }
                            }
                            this.locale.to(src, "commands.drop.nonexist");
                            return CommandResult.success();
                        })
                        .build(), "name")
                .build();
    }

}