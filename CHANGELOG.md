## Changelog
Not a "proper" versioned changelog, just a list of the changes from Minestom master.
Some of these are pending, some deserve PRs, others are just minor tweaks

* **breaking** Delete extensions (`mworzala/Minestom` @ `no_more_extensions`)
* **breaking** Block face in digging events (`mworzala/Minestom` @ `block_break_face`)
* **breaking** Add cursor position to block place and neighbor updates (`Moulberry/Minestom` @ `block_placement_rewrite_2`)
* Change `Entity#getInstance` to @UnknownNullability
* Support custom component translator for serverside translation
* **breaking** Replace permission system with a simple user pluggable alternative
* **breaking** Remove tinylog and MinestomTerminal implementation
* Add `Tag.Transient`
* Optionally allow multiple parents in event nodes
* **breaking** Add sender to argument parsing chain
  * This allows for argument parsing based on the sender, such as in argument map. This was already present for suggestions, but not for parsing.
  * This is a breaking change because it changes the signature of `Argument#parse`, but most use cases should not be affected.
    Support has been maintained for the old argument map signature, so only completely custom arguments will be affected.