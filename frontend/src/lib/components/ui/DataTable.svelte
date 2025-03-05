<script lang="ts" generics="TData, TValue">
	import { type ColumnDef, getCoreRowModel, getFilteredRowModel, type VisibilityState } from '@tanstack/table-core';
	import { createSvelteTable, FlexRender } from '$lib/components/ui/data-table/';
	// noinspection ES6UnusedImports
	import * as Table from '$lib/components/ui/table';
	// noinspection ES6UnusedImports
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.js';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';

	type DataTableProps<TData, TValue> = {
		columns: ColumnDef<TData, TValue>[];
		data: TData[];
		initialColumnVisibility: Record<string, boolean>;
		onRowSelect?: ((data: TData) => void) | null;
	};

	let { data, columns, initialColumnVisibility, onRowSelect = null }: DataTableProps<TData, TValue> = $props();

	let rowCursor = $derived.by(() => {
		if (onRowSelect) {
			return "cursor-pointer";
		} else {
			return "cursor-default";
		}
	});

	let columnVisibility = $state<VisibilityState>(initialColumnVisibility);
	let globalFilter = $state<any>([]);

	const table = createSvelteTable({
		get data() {
			return data;
		},
		columns,
		getCoreRowModel: getCoreRowModel(),
		getFilteredRowModel: getFilteredRowModel(),
		onColumnVisibilityChange: (updater) => {
			if (typeof updater === 'function') {
				columnVisibility = updater(columnVisibility);
			} else {
				columnVisibility = updater;
			}
		},
		onGlobalFilterChange: (updater) => {
			if (typeof updater === 'function') {
				globalFilter = updater(globalFilter);
			} else {
				globalFilter = updater;
			}
		},
		state: {
			get columnVisibility() {
				return columnVisibility;
			},
			get globalFilter() {
				return globalFilter;
			}
		},
		globalFilterFn: 'includesString'
	});
</script>

<div class="max-w-screen-2xl">
	<div class="container flex items-center py-4">
		<Input
			placeholder="Search..."
			value=""
			onchange={(e) => {
				table.setGlobalFilter(String(e.currentTarget.value));
      }}
			oninput={(e) => {
				table.setGlobalFilter(String(e.currentTarget.value));
      }}
			class="max-w-sm mr-2"
			autocorrect="off"
		/>
		<DropdownMenu.Root>
			<DropdownMenu.Trigger>
				{#snippet child({ props })}
					<Button {...props} variant="outline" class="ml-auto normal-case">columns</Button>
				{/snippet}
			</DropdownMenu.Trigger>
			<DropdownMenu.Content align="end">
				{#each table
					.getAllColumns()
					.filter((col) => col.getCanHide()) as column (column.id)}
					<DropdownMenu.CheckboxItem
						class="capitalize"
						bind:checked={() => column.getIsVisible(),
            (v) => column.toggleVisibility(!!v)}
					>
						{column.columnDef.header || column.id}
					</DropdownMenu.CheckboxItem>
				{/each}
			</DropdownMenu.Content>
		</DropdownMenu.Root>
	</div>
	<div class="sm:container">
		<div class="border sm:rounded-md">
			<Table.Root>
				<Table.Header>
					{#each table.getHeaderGroups() as headerGroup (headerGroup.id)}
						<Table.Row>
							{#each headerGroup.headers as header (header.id)}
								<Table.Head>
									{#if !header.isPlaceholder}
										<FlexRender
											content={header.column.columnDef.header}
											context={header.getContext()}
										/>
									{/if}
								</Table.Head>
							{/each}
						</Table.Row>
					{/each}
				</Table.Header>
				<Table.Body>
					{#each table.getRowModel().rows as row (row.id)}
						<Table.Row data-state={row.getIsSelected() && "selected"} class={rowCursor} onclick={() => onRowSelect?.(row.original)}>
							{#each row.getVisibleCells() as cell (cell.id)}
								<Table.Cell>
									<FlexRender
										content={cell.column.columnDef.cell}
										context={cell.getContext()}
									/>
								</Table.Cell>
							{/each}
						</Table.Row>
					{:else}
						<Table.Row>
							<Table.Cell colspan={columns.length} class="h-24 text-center">
								No results.
							</Table.Cell>
						</Table.Row>
					{/each}
				</Table.Body>
			</Table.Root>
		</div>
	</div>
</div>