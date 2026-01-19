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

This block connects a Replication network and an ME network.

It can store matter from a Replication network into an ME network, and it can also extract matter back out of the ME network to the Replication network.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="structures/replication_connector_basic.snbt" />
  <IsometricCamera yaw="195" pitch="30" />
</GameScene>

## Autocrafting

Patterns stored in a Chip Storage are registered as craftable in the ME network.
The Replicator output is inserted into the ME network via the ME Replication Connector.

<GameScene zoom="6" interactive={true}>
  <ImportStructure src="structures/auto_craft.snbt" />
  <IsometricCamera yaw="195" pitch="30" />

  <BlockAnnotation color="#dddddd" x="0" y="0" z="1">
    Autocrafting uses matter stored in ME Matter Storage Cells.
  </BlockAnnotation>
</GameScene>
