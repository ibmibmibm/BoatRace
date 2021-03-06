package syam.BoatRace;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.PluginDescriptionFile;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

import syam.BoatRace.Command.BaseCommand;
import syam.BoatRace.Command.CreateCommand;
import syam.BoatRace.Command.HelpCommand;
import syam.BoatRace.Command.JoinCommand;
import syam.BoatRace.Command.ReadyCommand;
import syam.BoatRace.Command.SelectRaceCommand;
import syam.BoatRace.Command.SetCommand;
import syam.BoatRace.Command.StartCommand;
import syam.BoatRace.Listener.BRPlayerListener;
import syam.BoatRace.Listener.BRVehicleListener;
import syam.BoatRace.Race.BRBoat;
import syam.BoatRace.Race.Race;
import syam.BoatRace.Race.RaceFileManager;
import syam.BoatRace.Race.RaceManager;


public class BoatRace extends JavaPlugin{
	/**
	 * TODO:
	 * w.spawn(spawnBoat, Boat.class);
	 *
	 */
	// ** Logger **
	public final static Logger log = Logger.getLogger("Minecraft");
	public final static String logPrefix = "[BoatRace] ";
	public final static String msgPrefix = "&6[BoatRace] &f";

	// ** Listeners **
	private final BRVehicleListener vehicleListener = new BRVehicleListener(this);
	private final BRPlayerListener playerListener = new BRPlayerListener(this);

	// ** Commands **
	public static List<BaseCommand> commands = new ArrayList<BaseCommand>();

	// Private classes
	private ConfigurationManager config;
	private RaceManager rm;
	private RaceFileManager rfm;

	// ** Variable **
	// 存在するレース <String 一意のレースID, Game>
	public HashMap<String, Race> races = new HashMap<String, Race>();


	// ** Instance **
	private static BoatRace instance;

	/**
	 * プラグイン起動処理
	 */
	public void onEnable(){
		instance  = this;
		PluginManager pm = getServer().getPluginManager();
		config = new ConfigurationManager(this);
		// loadconfig
		try{
			config.loadConfig(true);
		}catch (Exception ex){
			log.warning(logPrefix+"an error occured while trying to load the config file.");
			ex.printStackTrace();
		}

		// プラグインを無効にした場合進まないようにする
		if (!pm.isPluginEnabled(this)){
			return;
		}

		// Regist Listeners
		pm.registerEvents(vehicleListener, this);
		pm.registerEvents(playerListener, this);

		// コマンド登録
		registerCommands();

		// マネージャ
		rm = new RaceManager(this);
		rfm = new RaceFileManager(this);

		// レースゲーム読み込み
		rfm.loadGames();

		// メッセージ表示
		PluginDescriptionFile pdfFile=this.getDescription();
		log.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is enabled!");
	}

	/**
	 * プラグイン停止処理
	 */
	public void onDisable(){
		// 開始中のゲームをすべて終わらせる
		for (Race race : races.values()){
			if (race.isStarting()){
				race.cancelTimerTask();
				race.timeout();
				//race.log("Race game finished because disabling plugin..");
			}
		}

		// ゲームデータ保存
		if (rfm != null){
			rfm.saveGames();
		}

		// メッセージ表示
		PluginDescriptionFile pdfFile=this.getDescription();
		log.info("["+pdfFile.getName()+"] version "+pdfFile.getVersion()+" is disabled!");
	}

	/**
	 * コマンドを登録
	 */
	private void registerCommands(){
		// Intro Commands
		commands.add(new HelpCommand());
		commands.add(new JoinCommand());

		// Start Commands
		commands.add(new ReadyCommand());
		commands.add(new StartCommand());

		// Admin Commands
		commands.add(new CreateCommand());
		commands.add(new SetCommand());
		commands.add(new SelectRaceCommand());

	}

	/**
	 * コマンドが呼ばれた
	 */
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String args[]){
		if (cmd.getName().equalsIgnoreCase("boatrace")){
			if(args.length == 0){
				// 引数ゼロはヘルプ表示
				args = new String[]{"help"};
			}

			outer:
			for (BaseCommand command : commands.toArray(new BaseCommand[0])){
				String[] cmds = command.name.split(" ");
				for (int i = 0; i < cmds.length; i++){
					if (i >= args.length || !cmds[i].equalsIgnoreCase(args[i])){
						continue outer;
					}
					// 実行
					return command.run(this, sender, args, commandLabel);
				}
			}
			// 有効コマンドなし ヘルプ表示
			new HelpCommand().run(this, sender, args, commandLabel);
			return true;
		}
		return false;
	}

	/* getter */

	/**
	 * レースを返す
	 * @param gameName
	 * @return Game
	 */
	public Race getGame(String gameName){
		if (!races.containsKey(gameName)){
			return null;
		}else{
			return races.get(gameName);
		}
	}

	/**
	 * レースマネージャーを返す
	 * @return RaceManager
	 */
	public RaceManager getManager(){
		return rm;
	}

	/**
	 * レースファイルマネージャーを返す
	 * @return RaceFileManager
	 */
	public RaceFileManager getFileManager(){
		return rfm;
	}

	/**
	 * 設定マネージャを返す
	 * @return ConfigurationManager
	 */
	public ConfigurationManager getConfigs() {
		return config;
	}

	/**
	 * インスタンスを返す
	 * @return VoteBanインスタンス
	 */
	public static BoatRace getInstance(){
		return instance;
	}
}
