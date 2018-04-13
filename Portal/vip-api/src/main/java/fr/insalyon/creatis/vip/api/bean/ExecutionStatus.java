/*
 * Copyright and authors: see LICENSE.txt in base repository.
 *
 * This software is a web portal for pipeline execution on distributed systems.
 *
 * This software is governed by the CeCILL-B license under French law and
 * abiding by the rules of distribution of free software.  You can  use,
 * modify and/ or redistribute the software under the terms of the CeCILL-B
 * license as circulated by CEA, CNRS and INRIA at the following URL
 * "http://www.cecill.info".
 *
 * As a counterpart to the access to the source code and  rights to copy,
 * modify and redistribute granted by the license, users are provided only
 * with a limited warranty  and the software's author,  the holder of the
 * economic rights,  and the successive licensors  have only  limited
 * liability.
 *
 * In this respect, the user's attention is drawn to the risks associated
 * with loading,  using,  modifying and/or developing or reproducing the
 * software by the user in light of its specific status of free software,
 * that may mean  that it is complicated to manipulate,  and  that  also
 * therefore means  that it is reserved for developers  and  experienced
 * professionals having in-depth computer knowledge. Users are therefore
 * encouraged to load and test the software's suitability as regards their
 * requirements in conditions enabling the security of their systems and/or
 * data to be ensured and,  more generally, to use and operate it in the
 * same conditions as regards security.
 *
 * The fact that you are presently reading this means that you have had
 * knowledge of the CeCILL-B license and that you accept its terms.
 */
package fr.insalyon.creatis.vip.api.bean;

import com.fasterxml.jackson.annotation.*;

import javax.xml.bind.annotation.*;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;


/**
 * Created by abonnet on 8/4/16.
 */
@XmlType(name="ExecutionStatus")
public enum ExecutionStatus {

    @XmlEnumValue("Initializing")
    INITIALIZING("Initializing"),
    @XmlEnumValue("Ready")
    READY("Ready"),
    @XmlEnumValue("Running")
    RUNNING("Running"),
    @XmlEnumValue("Finished")
    FINISHED("Finished"),
    @XmlEnumValue("InitializationFailed")
    INITIALIZATION_FAILED("InitializationFailed"),
    @XmlEnumValue("ExecutionFailed")
    EXECUTION_FAILED("ExecutionFailed"),
    @XmlEnumValue("Unknown")
    UNKOWN("Unknown"),
    @XmlEnumValue("Killed")
    KILLED("Killed");

    private String restLabel;

    ExecutionStatus(String restLabel) {
        this.restLabel = restLabel;
    }

    @JsonCreator
    public static ExecutionStatus fromRestLabel(String restlabel) {
        for (ExecutionStatus status : values()) {
            if (status.restLabel.equals(restlabel)) { return status; }
        }
        throw new IllegalArgumentException("Unknown execution status : " + restlabel);
    }

    @JsonValue
    public String getRestLabel() {
        return restLabel;
    }
}
