# Applied-Replicatics

[日本語](README_JP.md) | [English](README.md)

This is an add-on that integrates Applied Energistics 2 and Replication.

## Features

### Matter integration into AE2

Provides functionality to handle Replication's Matter within Applied Energistics 2.

This is not a pseudo-integration via custom blocks from existing mods; instead, it is a full integration that leverages AE2's standard features.

### Matter Storage Cells

Adds Storage Cells for storing Matter.

Compatible with MEGA Cells, with capacities ranging from 1k to 256M.

The basic behavior is the same as other Storage Cells, but 256 Matter consumes 1 byte.

> [!NOTE]
> To use this, you need an ME Replication Connector to exchange Matter between the Replication network and the ME Network.
>
> Note that it is not compatible with blocks from other mods that provide similar functionality.

### ME Replication Connector

A block that connects the Replication network and the ME Network bidirectionally.

You can store Matter from the Replication network into the ME Network, and also extract it back to the Replication network.

Patterns stored in the Chip Storage are registered to the ME Network as "craftable".

When autocrafting, results produced by Replicators are imported into the ME Network via the ME Replication Connector.

---

## License

The source code of this Mod is licensed under the [GNU Lesser General Public License v3.0 (LGPL-3.0)](https://www.gnu.org/licenses/lgpl-3.0.html).

The source code is available from the [GitHub repository](https://github.com/Lapis256/Applied-Replicatics).

Assets are licensed
under [Creative Commons Attribution-NonCommercial-ShareAlike 3.0 (CC BY-NC-SA 3.0)](https://creativecommons.org/licenses/by-nc-sa/3.0/).

### Dependencies and their licenses

This Mod depends on the following projects:

- [Applied-Energistics-2](https://github.com/AppliedEnergistics/Applied-Energistics-2): licensed
  under [LGPL-3.0](https://www.gnu.org/licenses/lgpl-3.0.html)
- [Replication](https://github.com/Buuz135/Replication): licensed under [MIT](https://github.com/Buuz135/Replication/blob/1.21/LICENSE)
- [MEGACells](https://github.com/62832/MEGACells): licensed under [LGPL-3.0](https://www.gnu.org/licenses/lgpl-3.0.html)

### Assets and their licenses

Textures in this Mod are derived from the following projects:

- [Applied-Energistics-2](https://github.com/AppliedEnergistics/Applied-Energistics-2): licensed
  under [CC BY-NC-SA 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/)
- [Replication](https://github.com/Buuz135/Replication): licensed under [MIT](https://github.com/Buuz135/Replication/blob/1.21/LICENSE)
- [MEGACells](https://github.com/62832/MEGACells): licensed under [CC BY-NC-SA 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/)

Therefore, as stated above, this Mod's assets are licensed under [CC BY-NC-SA 3.0](https://creativecommons.org/licenses/by-nc-sa/3.0/), and any usage
must comply with the terms of this license.
