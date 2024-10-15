<p align="left">
  <img height="100" src="https://i.imgur.com/xeBRDpy.jpeg">
  <br> <br>
  <a href="https://www.codacy.com/gh/NivixX/NDatabase/dashboard?utm_source=github.com&amp;utm_medium=referral&amp;utm_content=NDatabase/NivixX&amp;utm_campaign=Badge_Grade"><img src="https://app.codacy.com/project/badge/Grade/521e578f30d64d7d9e4d4eb30057c086"/></a>
  <a><img alt="Issues" src="https://img.shields.io/github/issues/NivixX/NDatabase"></a>
  <a><img alt="Closed issues" src="https://img.shields.io/github/issues-closed/NivixX/NDatabase"></a>
  <a><img alt="Forks" src="https://img.shields.io/github/forks/NivixX/NDatabase"></a>
  <a><img alt="Stars" src="https://img.shields.io/github/stars/NivixX/NDatabase"></a>
  <a><img alt="jitpack" src="https://jitpack.io/v/NivixX/NDatabase.svg"></a>
  <a><img alt="License" src="https://img.shields.io/github/license/NivixX/NDatabase"></a>
  <a><img alt="Visitor count" src="https://visitor-badge.glitch.me/badge?page_id=nivixx.ndatabase"></a>  
</p>

# NDatabase 

NDatabase is a lightweight and easy to use **indexed**
[key-value store](https://en.wikipedia.org/wiki/Key%E2%80%93value_database) database framework mainly aimed for minecraft servers and is multi-platform (currently supporting Bukkit / Spigot servers).
It can be used as a plugin, so you can install it once and use it everywhere without having to configure a database and duplicate connection pool each time you develop a new plugin. The API provide a fluent way to handle Async data fetch and server
main thread callback with [async to sync](#fluent-async-to-sync-API) mechanism. NDatabase can support multiple databases (currently MySQL, MariaDB, SQLite, and MongoDB implementation).
NDatabase can support java 8 from 18 and higher and all minecraft server version (tested from 1.8 to 1.19+).

[**NDatabase WIKI**](https://github.com/NivixX/NDatabase/wiki) - [Spigot page](https://www.spigotmc.org/resources/ndatabase-fast-data-model-creation-powerful-async-sync-api.107793/)

## Benefits of using NDatabase
* **Fast to use, you don't have to write any Repository class or write SQL:** this framework is designed in a way that you just have to create your data model object (DTO) and a fully usable repository will be created automatically. See [NDatabase Installation & Quickstart](https://github.com/NivixX/NDatabase/wiki/C.-Installation-&-Quickstart-in-5-minutes)
* **Install once, use it everywhere:** It's obvious that a server always have a lot of plugins, most of them require a database, and you need to re-implement, and configure your database for every plugin. Connection pool duplication cost a lot of resources. With NDatabase, you just install the plugin once, and you can use the API in every plugins without configuration needed.
* **Indexed key-value store:** by design, a key-value store is very easy and fast to operate but is not indexed. But in some case we really need to retrieve data by field's value. NDatabase provide you a very easy way to index some of your fields and query your data. find more infos [how key-value store are indexed](https://github.com/NivixX/NDatabase/wiki/F.-How-key-value-store-are-indexed-%3F)
* **Fluent Async to Sync API:** You are probably aware that you should never do database and I/O operations in the minecraft main-thread, NDatabase natively expose an API that can be used to retrieve data asynchronously and consume in synchronously. 
* **Database type agnostic:** You can develop your plugin once with NDatabase, and multiple database types will be supported through the same API. If you are a plugin creator, and you sell plugins it's  very convenient because you don't have to care about if your customer use MongoDB, MySQL etc. 
## How does it work & API usage
If you want to use NDatabase for your minecraft server, you can install it as a plugin easily, see [NDatabase Installation](https://github.com/NivixX/NDatabase/wiki/C.-Installation-&-Quickstart-in-5-minutes).
When NDatabase is running on your server, Creating the schema and repository for your data model is very easy, you can actually do that with one line of code.


```java
Repository<UUID, PlayerStats> repository = NDatabase.api().getOrCreateRepository(PlayerStats.class);
```
Your repository is now ready, and you can now use it for operations such as insert, update, delete, get, find, etc. Note that you can recall the same method to get your repository from anywhere as the repository instance for this class type is cached.

Here is an overview about how it works and how can it be used with multiple plugins.

<img src="https://i.imgur.com/K6Q1lBo.jpg" alt="drawing" height="240"/>

### Fluent async to sync API
As you may know, a minecraft server has a __*main thread*__ which handle the logic game tick and synchronisation, the server can tick up to 20 times per seconds (50 ms per tick) which mean, if you are doing heavy process in the main thread and it lead the server to take more than 50 ms to tick, your server will __*lag*__ and tick less often.

That's why you should always process heavy task and I/O task __*asynchronously*__, but there is another issue, you can't/should not mute the game state (eg: call Bukkit methods) asynchronously because it will __*break the synchronization*__ or even crash your server. Most of server software will simply prevent you from doing that.

In the scenario where you want to retrieve data asynchronously and use it inside your game context, you can do that by using the bukkit __*scheduler*__. The idea is to get the data in another thread and then schedule in the main thread, a task that is consuming your retrived data. It's doable by using the Bukkit methods but NDatabase provide you a fluent API to do that.

> Async and Sync examples:
```java
Repository<String, BlockDTO> blockRepository = NDatabase.api().getOrCreateRepository(BlockDTO.class);
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
            loadPlayer(playerDTO);
        });
```

> Query Example : Get best players, which have score >= 100 or a specific discord id
```java
List<PlayerData> bestPlayers = repository.find(NQuery.predicate("$.statistics.score >= 100 || $.discordId == 3432487284963298"));
```

<img src="https://i.imgur.com/q43cdhp.jpg" alt="drawing"/>

* **Async to sync**: in the first example we retrieve the data of a block asynchronously, as we know we should not change the game state asynchronously, we give a consumer callback that will be scheduled and run in the main thread. This approach __*doesn't affect main thread's performances*__ as we retrieve the data in another thread.

* **Full async**: in the second example, we retrieve the data of a player who just connected to the server asynchronously and consume this data in the same async thread, because we don't necessarily have to do bukkit operation but just cache some informations, so all this can be done of the main thread. Keep in mind that you should use concurrent collections to avoid getting `ConcurrentModificationException`

## Documentation
NDatabase is designed to be fast and easy to use and still support value indexed as well. This framework will cover most of your use-case, but I recommend you to read the documentation to know about general best practices.

[**NDatabase WIKI**](https://github.com/NivixX/NDatabase/wiki)


## Build jar or API
`mvn clean install -DSkipTests`

It will create the complete jar in `ndatabase-packaging-jar/target` and the API in `ndatabase-api/target`

### Future objectives
WIP
- find((predicate)) parse predicate into a query that use index using bytecode manipulation
- migration mecanisms
