---
navigation:
    title: ME Replication Connector
    icon: replication_connector
    parent: index.md
item_ids:
    - replication_connector
---

# Applied Replicatics: ME Replication Connector

<BlockImage id="replication_connector" scale="4" />

複製ネットワークと MEネットワークを相互に接続するためのブロックです。

マターを、複製ネットワークから MEネットワークに保存したり、複製ネットワークから取り出したりすることができます。

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="structures/replication_connector_basic.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## 自動クラフト

チップ保管機に保存されたパターンは MEネットワークへ「自動クラフト可能」として登録されます。
複製機の結果は ME Replication Connector 経由で MEネットワークへ搬入されます。

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="structures/auto_craft.snbt" />
  <IsometricCamera yaw="195" pitch="30" />

  <BlockAnnotation color="#dddddd" x="0" y="0" z="1">
    ME マターストレージセルに保存されたマターを使って自動クラフトが行われます。
  </BlockAnnotation>
</GameScene>
