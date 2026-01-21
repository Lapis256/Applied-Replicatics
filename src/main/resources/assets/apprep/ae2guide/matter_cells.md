---
navigation:
    title: ME Matter Storage Cell
    icon: matter_cell_256k
    parent: index.md
item_ids:
    - matter_cell_housing
    - matter_cell_1k
    - matter_cell_4k
    - matter_cell_16k
    - matter_cell_64k
    - matter_cell_256k
    - mega_matter_cell_housing
    - matter_cell_1m
    - matter_cell_4m
    - matter_cell_16m
    - matter_cell_64m
    - matter_cell_256m
---

# Applied Replicatics: ME Matter Storage Cell

<Row>
  <ItemImage id="matter_cell_housing" scale="4"/>
  <ItemImage id="matter_cell_1k" scale="4"/>
  <ItemImage id="matter_cell_4k" scale="4"/>
  <ItemImage id="matter_cell_16k" scale="4"/>
  <ItemImage id="matter_cell_64k" scale="4"/>
  <ItemImage id="matter_cell_256k" scale="4"/>
</Row>

[Storage Cells](ae2:items-blocks-machines/storage_cells.md) that can store Replication matter.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="structures/matter_cells.snbt" />
  <IsometricCamera yaw="195" pitch="30" />

  <BoxAnnotation color="#dddddd" min="0 0 0" max="1 1 0.3">
    To make it appear, use a filled Matter Tank twice.
  </BoxAnnotation>
</GameScene>


The basic specs are the same as [Storage Cells](ae2:items-blocks-machines/storage_cells.md), but it consumes 1 byte per 256 matter.

As part of integration with MEGA Cells, MEGA Storage Cells are also available.

---

## Note

To exchange matter with a Replication network, you need the <ItemLink id="replication_connector" />.
Please note that it is not compatible with blocks from other mods that perform similar functions.
