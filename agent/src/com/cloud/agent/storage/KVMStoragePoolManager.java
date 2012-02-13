package com.cloud.agent.storage;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.cloud.agent.resource.computing.KVMHABase;
import com.cloud.agent.resource.computing.KVMHABase.PoolType;
import com.cloud.agent.resource.computing.KVMHAMonitor;
import com.cloud.agent.storage.KVMPhysicalDisk.PhysicalDiskFormat;
import com.cloud.storage.Storage.StoragePoolType;
import com.cloud.storage.StorageLayer;

public class KVMStoragePoolManager {
	private StorageAdaptor _storageAdaptor;
	private KVMHAMonitor _haMonitor;
	private final Map<String, Object> _storagePools = new ConcurrentHashMap<String, Object>();

	private void addStoragePool(String uuid) {
		synchronized (_storagePools) {
			if (!_storagePools.containsKey(uuid)) {
				_storagePools.put(uuid, new Object());
			}
		}
	}

	public KVMStoragePoolManager(StorageLayer storagelayer, KVMHAMonitor monitor) {
		this._storageAdaptor = new LibvirtStorageAdaptor(storagelayer);
		this._haMonitor = monitor;
	}

	public KVMStoragePool getStoragePool(String uuid) {
		return this._storageAdaptor.getStoragePool(uuid);
	}

	public KVMStoragePool getStoragePoolByURI(String uri) {
		return this._storageAdaptor.getStoragePoolByUri(uri);
	}

	public KVMStoragePool createStoragePool(String name, String host,
			String path, StoragePoolType type) {
		KVMStoragePool pool = this._storageAdaptor.createStoragePool(name,
				host, path, type);
		if (type == StoragePoolType.NetworkFilesystem) {
			KVMHABase.NfsStoragePool nfspool = new KVMHABase.NfsStoragePool(
					pool.getUuid(), host, path, pool.getLocalPath(),
					PoolType.PrimaryStorage);
			_haMonitor.addStoragePool(nfspool);
		}
		addStoragePool(pool.getUuid());
		return pool;
	}

	public boolean deleteStoragePool(String uuid) {
		_haMonitor.removeStoragePool(uuid);
		this._storageAdaptor.deleteStoragePool(uuid);
		_storagePools.remove(uuid);
		return true;
	}

	public KVMPhysicalDisk createDiskFromTemplate(KVMPhysicalDisk template,
			String name, KVMStoragePool destPool) {
		if (destPool.getType() == StoragePoolType.CLVM) {
			return this._storageAdaptor.createDiskFromTemplate(template, name,
					KVMPhysicalDisk.PhysicalDiskFormat.RAW, template.getSize(),
					destPool);
		} else {
			return this._storageAdaptor.createDiskFromTemplate(template, name,
					KVMPhysicalDisk.PhysicalDiskFormat.QCOW2,
					template.getSize(), destPool);
		}
	}

	public KVMPhysicalDisk createTemplateFromDisk(KVMPhysicalDisk disk,
			String name, PhysicalDiskFormat format, long size,
			KVMStoragePool destPool) {
		return this._storageAdaptor.createTemplateFromDisk(disk, name, format,
				size, destPool);
	}

	public KVMPhysicalDisk copyPhysicalDisk(KVMPhysicalDisk disk, String name,
			KVMStoragePool destPool) {
		return this._storageAdaptor.copyPhysicalDisk(disk, name, destPool);
	}

	public KVMPhysicalDisk createDiskFromSnapshot(KVMPhysicalDisk snapshot,
			String snapshotName, String name, KVMStoragePool destPool) {
		return this._storageAdaptor.createDiskFromSnapshot(snapshot,
				snapshotName, name, destPool);
	}

	public KVMPhysicalDisk getPhysicalDiskFromUrl(String url) {
		return this._storageAdaptor.getPhysicalDiskFromURI(url);
	}
}
