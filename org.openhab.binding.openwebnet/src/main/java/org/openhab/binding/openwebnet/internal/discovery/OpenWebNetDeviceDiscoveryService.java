/**
 * Copyright (c) 2010-2019 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.openwebnet.internal.discovery;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.smarthome.config.discovery.AbstractDiscoveryService;
import org.eclipse.smarthome.config.discovery.DiscoveryResult;
import org.eclipse.smarthome.config.discovery.DiscoveryResultBuilder;
import org.eclipse.smarthome.core.thing.ThingTypeUID;
import org.eclipse.smarthome.core.thing.ThingUID;
import org.openhab.binding.openwebnet.OpenWebNetBindingConstants;
import org.openhab.binding.openwebnet.handler.OpenWebNetBridgeHandler;
import org.openwebnet.OpenDeviceType;
import org.openwebnet.OpenNewDeviceListener;
import org.openwebnet.message.BaseOpenMessage;
import org.openwebnet.message.CEN;
import org.openwebnet.message.OpenMessageFactory;
import org.openwebnet.message.Who;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link OpenWebNetDeviceDiscoveryService} is responsible for discovering OpenWebNet devices connected to a
 * bridge/gateway
 *
 * @author Massimo Valla - Initial contribution
 */

public class OpenWebNetDeviceDiscoveryService extends AbstractDiscoveryService implements OpenNewDeviceListener {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES = OpenWebNetBindingConstants.DEVICE_SUPPORTED_THING_TYPES;

    private final static int SEARCH_TIME = 60;

    // private DiscoveryServiceCallback discoveryServiceCallback;

    private final Logger logger = LoggerFactory.getLogger(OpenWebNetDeviceDiscoveryService.class);
    private final OpenWebNetBridgeHandler bridgeHandler;
    private final ThingUID bridgeUID;

    public OpenWebNetDeviceDiscoveryService(OpenWebNetBridgeHandler handler) {
        super(SEARCH_TIME);
        bridgeHandler = handler;
        bridgeUID = handler.getThing().getUID();
        logger.debug("==OWN:DeviceDiscovery== constructor for bridge: {}", bridgeUID);
    }

    // @Override
    // public void setDiscoveryServiceCallback(DiscoveryServiceCallback discoveryServiceCallback) {
    // this.discoveryServiceCallback = discoveryServiceCallback;
    // }

    @Override
    public Set<ThingTypeUID> getSupportedThingTypes() {
        logger.debug("==OWN:DeviceDiscovery== getSupportedThingTypes()");
        return OpenWebNetDeviceDiscoveryService.SUPPORTED_THING_TYPES;
    }

    @Override
    protected void startScan() {
        logger.info("==OWN:DeviceDiscovery== ------ startScan() - SEARCHING for DEVICES on bridge {} ({})...",
                bridgeHandler.getThing().getLabel(), bridgeUID);
        bridgeHandler.searchDevices(this);
    }

    @Override
    protected void stopScan() {
        logger.debug("==OWN:DeviceDiscovery== ------ stopScan()");
        bridgeHandler.scanStopped();
    }

    @Override
    public void abortScan() {
        logger.debug("==OWN:DeviceDiscovery== ------ abortScan()");
        bridgeHandler.scanStopped();
    }

    @Override
    public void onNewDevice(String where, OpenDeviceType deviceType, BaseOpenMessage msg) {
        try {
            newDiscoveryResult(where, deviceType, msg);
        } catch (Exception e) {
            logger.error("==OWN:DeviceDiscovery== Exception {} while discovering new device:  WHERE={}, deviceType={}",
                    e.getMessage(), where, deviceType);
        }
    }

    /**
     * Create and notify to Inbox a new DiscoveryResult based on where, OpenDeviceType and BaseOpenMessage (optional)
     *
     * @param where      the discovered device's address (WHERE)
     * @param deviceType {@link OpenDeviceType} of the discovered device
     * @param message    the OWN message received that identified the device (optional)
     */
    public void newDiscoveryResult(String where, OpenDeviceType deviceType, BaseOpenMessage baseMsg) {
        logger.info("==OWN:DeviceDiscovery== newDiscoveryResult() WHERE={}, deviceType={}", where, deviceType);
        ThingTypeUID thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_DEVICE; // generic device
        String thingLabel = OpenWebNetBindingConstants.THING_LABEL_DEVICE;
        Who deviceWho = Who.DEVICE_DIAGNOSTIC; // FIXME
        if (deviceType != null) {
            switch (deviceType) {
                case ZIGBEE_ON_OFF_SWITCH: {
                    thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_ON_OFF_SWITCH;
                    thingLabel = OpenWebNetBindingConstants.THING_LABEL_ON_OFF_SWITCH;
                    deviceWho = Who.LIGHTING;
                    break;
                }
                case ZIGBEE_DIMMER_SWITCH: {
                    thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_DIMMER;
                    thingLabel = OpenWebNetBindingConstants.THING_LABEL_DIMMER;
                    deviceWho = Who.LIGHTING;
                    break;
                }
                case SCS_ON_OFF_SWITCH: {
                    thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_ON_OFF_SWITCH;
                    thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_ON_OFF_SWITCH;
                    deviceWho = Who.LIGHTING;
                    break;
                }
                case SCS_DIMMER_SWITCH: {
                    thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_DIMMER;
                    thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_DIMMER;
                    deviceWho = Who.LIGHTING;
                    break;
                }
                case SCS_SHUTTER_SWITCH:
                case SCS_SHUTTER_CONTROL: {
                    thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_AUTOMATION;
                    thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_AUTOMATION;
                    deviceWho = Who.AUTOMATION;
                    break;
                }
                case ZIGBEE_SHUTTER_SWITCH:
                case ZIGBEE_SHUTTER_CONTROL: {
                    thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_AUTOMATION;
                    thingLabel = OpenWebNetBindingConstants.THING_LABEL_AUTOMATION;
                    deviceWho = Who.AUTOMATION;
                    break;
                }
                case SCS_TEMP_SENSOR: {
                    thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_TEMP_SENSOR;
                    thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_TEMP_SENSOR;
                    deviceWho = Who.THERMOREGULATION;
                    break;
                }
                case SCS_THERMOSTAT: {
                    thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_THERMOSTAT;
                    thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_THERMOSTAT;
                    deviceWho = Who.THERMOREGULATION;
                    break;
                }
                case SCS_THERMO_CENTRAL_UNIT: {
                    thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_THERMO_CENTRAL_UNIT;
                    thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_THERMO_CENTRAL_UNIT;
                    deviceWho = Who.THERMOREGULATION;
                    break;
                }
                case SCS_ENERGY_CENTRAL_UNIT: {
                    thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_ENERGY_CENTRAL_UNIT;
                    thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_ENERGY_CENTRAL_UNIT;
                    deviceWho = Who.ENERGY_MANAGEMENT;
                    break;
                }
                case SCENARIO_CONTROL: {
                    thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_CEN_SCENARIO_CONTROL;
                    thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_CEN_SCENARIO_CONTROL;
                    deviceWho = Who.CEN_SCENARIO_SCHEDULER;
                    break;
                }
                case MULTIFUNCTION_SCENARIO_CONTROL: {
                    thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_CENPLUS_SCENARIO_CONTROL;
                    thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_CENPLUS_SCENARIO_CONTROL;
                    deviceWho = Who.CEN_PLUS_SCENARIO_SCHEDULER;
                    break;
                }
                case SCS_DRY_CONTACT_IR: {
                    thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_DRY_CONTACT_IR;
                    thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_DRY_CONTACT_IR;
                    deviceWho = Who.CEN_PLUS_SCENARIO_SCHEDULER;
                    break;
                }
                case SCS_AUXILIARY_TOGGLE_CONTROL: {
                    thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_BUS_AUX_TOGGLE;
                    thingLabel = OpenWebNetBindingConstants.THING_LABEL_BUS_AUX_TOGGLE;
                    deviceWho = Who.AUX;
                    break;
                }
                case ZIGBEE_AUXILIARY_ON_OFF_1_GANG_SWITCH: {
                    thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_AUX_SWITCH;
                    thingLabel = OpenWebNetBindingConstants.THING_LABEL_AUX_SWITCH;
                    deviceWho = Who.AUX;
                    break;
                }
                default:
                    logger.warn(
                            "==OWN:DeviceDiscovery== ***** device type {} is not supported, default to generic device (WHERE={}) *****",
                            deviceType, where);
            }
        }
        String ownId = bridgeHandler.ownIdFromWhoWhere(where, deviceWho.value().toString());
        // check existing thing
        if (bridgeHandler.ownIdExisting(ownId)) {
            logger.debug("==OWN:DeviceDiscovery== thing existing ownId={}", ownId);
        } else {
            String tId = bridgeHandler.thingIdFromWhere(where);
            ThingUID thingUID = new ThingUID(thingTypeUID, bridgeUID, tId);

            DiscoveryResult discoveryResult = null;
            // check if a device with same thingUID has been found already in discovery results
            // if (discoveryServiceCallback != null) {
            // discoveryResult = discoveryServiceCallback.getExistingDiscoveryResult(thingUID);
            // }

            String whereLabel = where;
            if (BaseOpenMessage.UNIT_02.equals(OpenMessageFactory.getUnit(where))) {
                logger.debug("==OWN:DeviceDiscovery== UNIT=02 found (WHERE={})", where);
                // if (discoveryResult != null) {
                logger.debug("==OWN:DeviceDiscovery== will remove previous result if exists");
                thingRemoved(thingUID); // remove previously discovered thing
                // re-create thingUID with new type
                thingTypeUID = OpenWebNetBindingConstants.THING_TYPE_ON_OFF_SWITCH_2UNITS;
                thingLabel = OpenWebNetBindingConstants.THING_LABEL_ON_OFF_SWITCH_2UNITS;
                thingUID = new ThingUID(thingTypeUID, bridgeUID, tId);
                whereLabel = whereLabel.replace("02#", "00#"); // replace unit '02' with all unit '00'
                logger.debug("==OWN:DeviceDiscovery== UNIT=02, switching type from {} to {}",
                        OpenWebNetBindingConstants.THING_TYPE_ON_OFF_SWITCH,
                        OpenWebNetBindingConstants.THING_TYPE_ON_OFF_SWITCH_2UNITS);
                // } else {
                // logger.warn("==OWN:DeviceDiscovery== discoveryResult empty after UNIT=02 discovery (WHERE={})",
                // where);
                // }
            }
            Map<String, Object> properties = new HashMap<>(2);
            properties.put(OpenWebNetBindingConstants.CONFIG_PROPERTY_WHERE, bridgeHandler.normalizeWhere(where));
            properties.put(OpenWebNetBindingConstants.PROPERTY_OWNID,
                    bridgeHandler.ownIdFromWhoWhere(bridgeHandler.normalizeWhere(where), deviceWho.value().toString()));

            if ((deviceType == OpenDeviceType.MULTIFUNCTION_SCENARIO_CONTROL
                    || deviceType == OpenDeviceType.SCENARIO_CONTROL) && baseMsg != null) {
                properties.put(OpenWebNetBindingConstants.CONFIG_PROPERTY_SCENARIO_BUTTONS,
                        ((CEN) baseMsg).getButtonNumber().toString());
            }
            if (thingTypeUID == OpenWebNetBindingConstants.THING_TYPE_DEVICE && baseMsg != null) { // generic thing,
                                                                                                   // let's
                                                                                                   // specify the WHO
                thingLabel = thingLabel + " (WHO=" + baseMsg.getWho() + ", WHERE=" + whereLabel + ")";
            } else {
                thingLabel = thingLabel + " (WHERE=" + whereLabel + ")";
            }
            discoveryResult = DiscoveryResultBuilder.create(thingUID).withThingType(thingTypeUID)
                    .withProperties(properties).withBridge(bridgeUID).withLabel(thingLabel).build();
            thingDiscovered(discoveryResult);
        }
    }

    public void activate() {
        logger.debug("==OWN:DeviceDiscovery== activate()");
        // TODO useful ?????
        // hueBridgeHandler.registerLightStatusListener(this);
    }

    @Override
    public void deactivate() {
        logger.debug("==OWN:DeviceDiscovery== deactivate()");
        // TODO useful?????
        // removeOlderResults(new Date().getTime());
        // hueBridgeHandler.unregisterLightStatusListener(this);
    }

    /**
     * Return where is Area
     *
     * @param String where
     * @return boolean
     */
    public boolean isArea(String where) {
        String wheretmp = where;
        int A = 0;
        if (where.indexOf("#4#") > 0) {
            wheretmp = where.substring(0, where.indexOf("#4#"));
        }
        if (wheretmp.indexOf("#") == -1) {
            A = Integer.parseInt(wheretmp);
            if ((A >= 1 && A <= 9) || A == 100) {
                logger.debug("==OWN:DeviceDiscovery== isArea() where:{} is Area", where);
                return true;
            }
        }
        return false;
    }

    /**
     * Return where is Group
     *
     * @param String where
     * @return boolean
     */
    public boolean isGroup(String where) {
        String wheretmp = where;
        int A = 0;
        if (where.indexOf("#4#") > 0) {
            wheretmp = where.substring(0, where.indexOf("#4#"));
        }
        if (wheretmp.indexOf('#') >= 0) {
            wheretmp = wheretmp.substring(wheretmp.indexOf('#') + 1, wheretmp.length());
            A = Integer.parseInt(wheretmp);
            if (A >= 1 && A <= 255) {
                logger.debug("==OWN:DeviceDiscovery== isGroup() where:{} is Group", where);
                return true;
            }
        }
        return false;
    }

    /**
     * Return where is General
     *
     * @param String where
     * @return boolean
     */
    public boolean isGeneral(String where) {
        String wheretmp = where;
        if (where.indexOf("#4#") > 0) {
            wheretmp = where.substring(0, where.indexOf("#4#"));
        }
        if (wheretmp.indexOf('#') == -1) {
            if (Integer.parseInt(wheretmp) == 0) {
                logger.debug("==OWN:DeviceDiscovery== isGeneral() where:{} is General", where);
                return true;
            }
        }
        return false;
    }
}
