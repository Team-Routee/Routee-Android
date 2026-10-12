package com.routee.android.feature.recordedit.component.main

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.routee.android.core.designsystem.R as DesignSystemR
import com.routee.android.core.designsystem.theme.RouteeTheme
import com.routee.android.feature.recordedit.R

/**
 * 해당 월에 기록이 없을 때 보여주는 빈 화면 (graphic_empty_data)
 */
@Composable
internal fun RecordEditEmptyContent(
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(RouteeTheme.spacing.Spacing24),
    ) {
        Image(
            painter = painterResource(DesignSystemR.drawable.graphic_empty_data_xl),
            contentDescription = null,
        )
        Text(
            text = stringResource(R.string.record_edit_main_empty_title),
            style = RouteeTheme.typography.labelM16,
            color = RouteeTheme.colors.grey300,
        )
    }
}

@Preview
@Composable
private fun RecordEditEmptyContentPreview() {
    RouteeTheme {
        RecordEditEmptyContent(modifier = Modifier.background(RouteeTheme.colors.bgPrimary))
    }
}
