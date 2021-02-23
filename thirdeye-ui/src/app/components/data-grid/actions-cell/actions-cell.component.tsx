import { Box, IconButton } from "@material-ui/core";
import { CellParams } from "@material-ui/data-grid";
import DeleteIcon from "@material-ui/icons/Delete";
import EditIcon from "@material-ui/icons/Edit";
import VisibilityIcon from "@material-ui/icons/Visibility";
import { toNumber } from "lodash";
import React, {
    FunctionComponent,
    ReactElement,
    useEffect,
    useState,
} from "react";
import { ActionsCellProps } from "./actions-cell.interfaces";

const ActionsCell: FunctionComponent<ActionsCellProps> = (
    props: ActionsCellProps
) => {
    const [rowId, setRowId] = useState(-1);
    const [align, setAlign] = useState("");

    useEffect(() => {
        // Input cell parameters changed
        setRowId(
            toNumber(props.params && props.params.row && props.params.row.id)
        );
        setAlign(
            props.params && props.params.colDef && props.params.colDef.align
        );
    }, [props.params]);

    const handleViewDetails = (): void => {
        props.onViewDetails && props.onViewDetails(rowId);
    };

    const handleEdit = (): void => {
        props.onEdit && props.onEdit(rowId);
    };

    const handleDelete = (): void => {
        props.onDelete && props.onDelete(rowId);
    };

    return (
        <Box textAlign={align} width="100%">
            {/* View details button */}
            {props.viewDetails && (
                <IconButton size="small" onClick={handleViewDetails}>
                    <VisibilityIcon fontSize="small" />
                </IconButton>
            )}

            {/* Edit button */}
            {props.edit && (
                <IconButton size="small" onClick={handleEdit}>
                    <EditIcon fontSize="small" />
                </IconButton>
            )}

            {/* Delete button */}
            {props.delete && (
                <IconButton size="small" onClick={handleDelete}>
                    <DeleteIcon fontSize="small" />
                </IconButton>
            )}
        </Box>
    );
};

export const actionsCellRenderer = (
    params: CellParams,
    showViewDetails?: boolean,
    showEdit?: boolean,
    showDelete?: boolean,
    onViewDetails?: (rowId: number) => void,
    onEdit?: (rowId: number) => void,
    onDelete?: (rowId: number) => void
): ReactElement => {
    return (
        <ActionsCell
            delete={showDelete}
            edit={showEdit}
            params={params}
            viewDetails={showViewDetails}
            onDelete={onDelete}
            onEdit={onEdit}
            onViewDetails={onViewDetails}
        />
    );
};
