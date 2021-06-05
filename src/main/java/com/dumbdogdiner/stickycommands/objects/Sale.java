package com.dumbdogdiner.stickycommands.objects;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.UUID;

import com.dumbdogdiner.stickyapi.common.util.NumberUtil;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import lombok.Getter;

/**
 * An object to represent a sale, this is mostly for convenience later...
 * FIXME is this necessary? check kotlin ver
 */
public class Sale {

    @Getter
    Integer saleId;

    @Getter
    UUID uniqueId;

    @Getter
    String username;

    @Getter
    Material item;

    @Getter
    Integer amount;

    @Getter
    Double price;

    @Getter
    Double newBalance;

    @Getter
    Timestamp date;

    public Sale(ResultSet result) {
        try {
            if (result == null)
                throw new NullPointerException("ResultSet is null, did the com.dumbdogdiner.stickycommands.database query execute successfully?");
            UUID uuid = UUID.fromString(result.getString("uuid"));
            // We want the username of the player... So we need to query the users table.
            this.saleId = result.getInt("id");
            this.uniqueId = uuid;
            this.username = result.getString("player_name");
            this.item = Material.getMaterial(result.getString("item"));
            this.amount = result.getInt("amount");
            this.price = Double.valueOf(NumberUtil.formatPrice(result.getDouble("item_worth")));
            this.newBalance = Double.valueOf(NumberUtil.formatPrice(result.getDouble("new_balance")));
            this.date = result.getTimestamp("time_sold");
        } catch(SQLException e) {
            e.printStackTrace();
        }
    }

    public Double getOldBalance() {
        return Double.valueOf(NumberUtil.formatPrice(this.newBalance - this.price));
    }
    
}