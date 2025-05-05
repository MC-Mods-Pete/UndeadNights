package net.petemc.undeadnights.command;

import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;

public class ModCommands {

    public static void registerCommands(){
        CommandRegistrationCallback.EVENT.register(SpawnHordeCommand::register);
        CommandRegistrationCallback.EVENT.register(HordeMobsCommand::register);
        CommandRegistrationCallback.EVENT.register(StatusCommand::register);
        CommandRegistrationCallback.EVENT.register(SetDefaultHordeCommand::register);
    }
}
