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

Replication のマターが保存可能な [ストレージセル](ae2:items-blocks-machines/storage_cells.md) です。

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="structures/matter_cells.snbt" />
  <IsometricCamera yaw="195" pitch="30" />

  <BoxAnnotation color="#dddddd" min="0 0 0" max="1 1 0.3">
    再現するには、中身の入ったマター用タンクを二回使用してください。
  </BoxAnnotation>
</GameScene>


基本的な仕様は [ストレージセル](ae2:items-blocks-machines/storage_cells.md) と同様ですが、256マターで 1バイト消費します。

MEGA Cells との連携要素として MEGAストレージセルも利用可能です。

---

## 注意
Replication ネットワークとマターをやり取りするためには、<ItemLink id="replication_connector" /> が必要です。
似たような働きをする別Mod のブロックとは互換性が無いことに注意してください。
