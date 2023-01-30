<p align="left">
  <img width="20%" height="20%" src="https://i.TODO.png">
  <br> <br>
  <a href="https://www.codacy.com/gh/NivixX/NDatabase/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=NDatabase/NivixX&amp;utm_campaign=Badge_Grade"><img src="https://app.codacy.com/project/badge/Grade/521e578f30d64d7d9e4d4eb30057c086"/></a>
  <a><img alt="Issues" src="https://img.shields.io/github/issues/NivixX/NDatabase"></a>
  <a><img alt="Forks" src="https://img.shields.io/github/forks/NivixX/NDatabase"></a>
  <a><img alt="Stars" src="https://img.shields.io/github/stars/NivixX/NDatabase"></a>
  <a><img alt="License" src="https://img.shields.io/github/license/NivixX/NDatabase"></a>
  <a><img alt="Authors" src="https://img.shields.io/badge/Authors-NivixX-blue"></a>  
</p>

# NDatabase
NDatabase is a lightweight and easy to use
[key-value store](https://en.wikipedia.org/wiki/Key%E2%80%93value_database) style database framework mainly aimed for minecraft servers and is multi-platform (currently supporting Spigot / Sponge servers).
It can be used as a plugin so you can install it once and use it everywhere without having to configure a database and duplicate connection pool each time you develop a new plugin. The API provide a fluent way to handle Async data fetch and server
main thread callback with [async to sync](#async-to-sync) mechanism. NDatabase can support multiple databases (currently MySQL and In-memory implementation).


## How does it work & API usage
If you want  ot use NDatabase for your minecraft server, you can install it as a plugin easily, see [installation & quickstart](#async-to-sync). Creating a repository for your data model is now very easy, you can actually do that with one line of code.

First let's define our data model, you just have to create a class which extends NEntity\<K> where K is the type of your key, currently UUID, String, Long, Integer types are supported.
```
@NTable(name = "player_statistics")
public class PlayerStats extends NEntity<UUID> {

    private int kill, death, blocMinedCount;
    // ... with getters / setters

    public PlayerStats() { } // note that you need a default constructor
}

```
Then, you just have to do one API call and NDatabase will create the database schema according to your database configuration and return you a repository for your data model.

```
Repository<UUID, PlayerStats> repository = NDatabase.api().getOrCreateRepository(PlayerStats.class);

```
Your repository is ready an you can now use it for operations such as insert, update, delete, get, find, etc. Note that you can recall the same method to get your repository from anywhere as the repository instance for this class type is cached.

Here is an overview about how it works and how can it be used with multiple plugins.

<img src="https://i.imgur.com/K6Q1lBo.jpg" alt="drawing" height="240"/>

### Fluent async to sync API
As you may know, a minecraft server has a __*main thread*__ which handle the logic game tick and synchronisation, the server can tick up to 20 times per seconds (50 ms per tick) which mean, if you are doing heavy process in the main thread and it lead the server to take more than 50 ms to tick, your server will __*lag*__ and tick less often.

That's why you should always process heavy task and I/O task __*asynchronously*__, but there is another issue, you can't/should not mute the game state (eg: call Bukkit methods) asynchronously because it will __*break the synchronization*__ or even crash your server. Most of server software will simply prevent you from doing that.

In the scenario where you want to retrieve data asynchronously and use it inside your game context, you can do that by using the bukkit __*scheduler*__. The idea is to get the data in another thread and then schedule in the main thread, a task that is consuming your retrived data. It's doable by using the Bukkit methods but NDatabase provide you a fluent API to do that.

Here is two examples:
```
Repository<UUID, BlockDTO> blockRepository = NDatabase.api().getOrCreateRepository(BlockDTO.class);
// Async to Sync (get data async and consume it in the main thread)
blockRepository.getAsync("id")
        .thenSync((bloc, exception) -> {
            if(exception != null) {
                // Handle exception
                return;
            }
            placeBlockInWorld(bloc);
        });

Repository<UUID, PlayerDTO> playerRepository = NDatabase.api().getOrCreateRepository(PlayerDTO.class);
// Full Async (get data async and consume it in the same async thread)
playerRepository.getAsync(joinedPlayer.getUUID())
        .thenAsync((playerDTO, exception) -> {
            if(exception != null) {
                // Handle exception
                return;
            }
            initPlayer(playerDTO);
        });
```
<img src="https://i.imgur.com/q43cdhp.jpg" alt="drawing"/>

* **Async to sync**: in the first example we retrieve the data of a block asynchronously, as we know we should not change the game state asynchronously, we give a consumer callback that will be scheduled and run in the main thread. This approach __*doesn't affect main thread's performances*__ as we retrieve the data in another thread.

* **Full async**: in the second example, we retrieve the data of a player who just connected to the server asynchronously and consume this data in the same async thread, because we don't necessarily have to do bukkit operation but just cache some informations, so all this can be done of the main thread. Keep in mind that you should use concurrent collections to avoid getting `ConcurrentModificationException`

### Repository API usage
#### Exception handling Sync
#### Exception handling Async

## Installation & quickStart

## Future objectives

