<script lang="ts" generics="TData, TValue">
	import {
		type ColumnDef,
		getCoreRowModel,
		getFilteredRowModel,
		getSortedRowModel,
		type SortingState,
		type VisibilityState
	} from '@tanstack/table-core';
	import { createSvelteTable, FlexRender } from '$lib/components/ui/data-table/';
	import type { Snippet } from 'svelte';
	// noinspection ES6UnusedImports
	import * as Table from '$lib/components/ui/table';
	// noinspection ES6UnusedImports
	import * as DropdownMenu from '$lib/components/ui/dropdown-menu/index.js';
	import { Button } from '$lib/components/ui/button';
	import { Input } from '$lib/components/ui/input';
	import { SlidersHorizontal } from 'lucide-svelte';

	type DataTableProps<TData, TValue> = {
		columns: ColumnDef<TData, TValue>[];
		data: TData[];
		initialColumnVisibility: Record<string, boolean>;
		initialSortingState: SortingState;
		onRowSelect?: ((data: TData) => void) | null;
		addButton?: Snippet | null;
	};

	let {
		data,
		columns,
		initialColumnVisibility,
		initialSortingState,
		onRowSelect = null,
		addButton = null
	}: DataTableProps<TData, TValue> = $props();

	let rowCursor = $derived.by(() => {
		if (onRowSelect) {
			return 'cursor-pointer';
		} else {
			return 'cursor-default';
		}
	});

	let columnVisibility = $state<VisibilityState>(initialColumnVisibility);
	/* eslint-disable  @typescript-eslint/no-explicit-any */
	let globalFilter = $state<any>([]);
	let sorting = $state<SortingState>(initialSortingState);

	const table = createSvelteTable({
		get data() {
			return data;
		},
		columns,
		getCoreRowModel: getCoreRowModel(),
		getFilteredRowModel: getFilteredRowModel(),
		getSortedRowModel: getSortedRowModel(),
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
		onSortingChange: (updater) => {
			if (typeof updater === 'function') {
				sorting = updater(globalFilter);
			} else {
				sorting = updater;
			}
		},
		state: {
			get columnVisibility() {
				return columnVisibility;
			},
			get globalFilter() {
				return globalFilter;
			},
			get sorting() {
				return sorting;
			}
		},
		globalFilterFn: 'includesString'
	});
</script>

<div class="max-w-(--breakpoint-2xl) sm:container">
	<div class="mx-4 flex items-center py-4 sm:mx-0">
		<Input
			placeholder="Search..."
			value=""
			onchange={(e) => {
				table.setGlobalFilter(String(e.currentTarget.value));
			}}
			oninput={(e) => {
				table.setGlobalFilter(String(e.currentTarget.value));
			}}
			class="mr-2 max-w-sm"
			autocorrect="off"
		/>
		<DropdownMenu.Root>
			<DropdownMenu.Trigger>
				{#snippet child({ props })}
					<Button {...props} variant="outline" class="mr-2 ml-auto normal-case">
						<SlidersHorizontal />
						<div>View</div>
					</Button>
				{/snippet}
			</DropdownMenu.Trigger>
			<DropdownMenu.Content align="end">
				<DropdownMenu.Label>Toggle columns</DropdownMenu.Label>
				<DropdownMenu.Separator />
				{#each table.getAllColumns().filter((col) => col.getCanHide()) as column (column.id)}
					<DropdownMenu.CheckboxItem
						class="capitalize"
						bind:checked={() => column.getIsVisible(), (v) => column.toggleVisibility(!!v)}
					>
						{column.columnDef.header || column.id}
					</DropdownMenu.CheckboxItem>
				{/each}
			</DropdownMenu.Content>
		</DropdownMenu.Root>
		{#if addButton}
			{@render addButton()}
		{/if}
	</div>
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
					<Table.Row
						data-state={row.getIsSelected() && 'selected'}
						class={rowCursor}
						onclick={() => onRowSelect?.(row.original)}
					>
						{#each row.getVisibleCells() as cell (cell.id)}
							<Table.Cell>
								<FlexRender content={cell.column.columnDef.cell} context={cell.getContext()} />
							</Table.Cell>
						{/each}
					</Table.Row>
				{:else}
					<Table.Row>
						<Table.Cell colspan={columns.length} class="h-24 text-center">No results.</Table.Cell>
					</Table.Row>
				{/each}
			</Table.Body>
		</Table.Root>
	</div>
</div>
